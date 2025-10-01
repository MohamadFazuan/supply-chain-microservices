package com.supply.chain.microservice.productservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Product Attribute Entity
 * Implements Entity-Attribute-Value (EAV) pattern for dynamic product attributes
 */
@Entity
@Table(name = "product_attributes",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_product_attribute",
        columnNames = {"product_id", "attribute_name"}
    ),
    indexes = {
        @Index(name = "idx_product", columnList = "product"),
        @Index(name = "idx_attribute_name", columnList = "attributeName"),
        @Index(name = "idx_searchable", columnList = "searchable")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    @Column(name = "attribute_name", nullable = false, length = 100)
    @NotBlank(message = "Attribute name is required")
    @Size(max = 100, message = "Attribute name must not exceed 100 characters")
    private String attributeName;

    @Column(name = "attribute_value", columnDefinition = "TEXT")
    private String attributeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribute_type")
    @Builder.Default
    private AttributeType attributeType = AttributeType.STRING;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_searchable")
    @Builder.Default
    private Boolean searchable = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Attribute data types
     */
    public enum AttributeType {
        STRING("String"),
        NUMBER("Number"),
        BOOLEAN("Boolean"),
        DATE("Date"),
        JSON("JSON");

        private final String displayName;

        AttributeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Get typed value based on attribute type
     */
    public Object getTypedValue() {
        if (attributeValue == null) return null;
        
        return switch (attributeType) {
            case STRING -> attributeValue;
            case NUMBER -> {
                try {
                    if (attributeValue.contains(".")) {
                        yield Double.parseDouble(attributeValue);
                    } else {
                        yield Long.parseLong(attributeValue);
                    }
                } catch (NumberFormatException e) {
                    yield attributeValue;
                }
            }
            case BOOLEAN -> Boolean.parseBoolean(attributeValue);
            case DATE -> {
                try {
                    yield java.time.LocalDate.parse(attributeValue);
                } catch (Exception e) {
                    yield attributeValue;
                }
            }
            case JSON -> attributeValue; // Return as string, parsing handled elsewhere
        };
    }

    /**
     * Set value with automatic type conversion
     */
    public void setTypedValue(Object value) {
        if (value == null) {
            this.attributeValue = null;
            return;
        }
        
        this.attributeValue = value.toString();
        
        // Auto-detect type if not set
        if (this.attributeType == null) {
            this.attributeType = detectType(value);
        }
    }

    /**
     * Detect attribute type from value
     */
    private AttributeType detectType(Object value) {
        if (value instanceof Number) {
            return AttributeType.NUMBER;
        } else if (value instanceof Boolean) {
            return AttributeType.BOOLEAN;
        } else if (value instanceof java.time.LocalDate || value instanceof java.time.LocalDateTime) {
            return AttributeType.DATE;
        } else if (value.toString().startsWith("{") || value.toString().startsWith("[")) {
            return AttributeType.JSON;
        } else {
            return AttributeType.STRING;
        }
    }

    /**
     * Check if attribute is numeric
     */
    public boolean isNumeric() {
        return attributeType == AttributeType.NUMBER;
    }

    /**
     * Check if attribute is boolean
     */
    public boolean isBoolean() {
        return attributeType == AttributeType.BOOLEAN;
    }

    /**
     * Check if attribute is date
     */
    public boolean isDate() {
        return attributeType == AttributeType.DATE;
    }

    /**
     * Check if attribute is JSON
     */
    public boolean isJson() {
        return attributeType == AttributeType.JSON;
    }

    /**
     * Get display value formatted for the attribute type
     */
    public String getDisplayValue() {
        if (attributeValue == null) return "";
        
        return switch (attributeType) {
            case BOOLEAN -> Boolean.parseBoolean(attributeValue) ? "Yes" : "No";
            case NUMBER -> {
                try {
                    if (attributeValue.contains(".")) {
                        yield String.format("%.2f", Double.parseDouble(attributeValue));
                    } else {
                        yield String.format("%,d", Long.parseLong(attributeValue));
                    }
                } catch (NumberFormatException e) {
                    yield attributeValue;
                }
            }
            default -> attributeValue;
        };
    }
}
