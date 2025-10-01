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
import java.util.ArrayList;
import java.util.List;

/**
 * Product Category Entity for hierarchical categorization
 * Supports multi-level categories for Tiong Nam Logistics products
 */
@Entity
@Table(name = "product_categories", indexes = {
    @Index(name = "idx_category_code", columnList = "categoryCode"),
    @Index(name = "idx_parent_category", columnList = "parentCategory"),
    @Index(name = "idx_level", columnList = "level"),
    @Index(name = "idx_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", unique = true, nullable = false, length = 50)
    @NotBlank(message = "Category code is required")
    @Size(max = 50, message = "Category code must not exceed 50 characters")
    private String categoryCode;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ProductCategory parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductCategory> subCategories = new ArrayList<>();

    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 0;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private CategoryStatus status = CategoryStatus.ACTIVE;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    /**
     * Get the full path of the category from root to this category
     */
    public String getCategoryPath() {
        if (parentCategory == null) {
            return name;
        }
        return parentCategory.getCategoryPath() + " > " + name;
    }

    /**
     * Check if this category is a root category
     */
    public boolean isRootCategory() {
        return parentCategory == null;
    }

    /**
     * Check if this category is a leaf category (has no subcategories)
     */
    public boolean isLeafCategory() {
        return subCategories.isEmpty();
    }

    /**
     * Get all descendant categories recursively
     */
    public List<ProductCategory> getAllDescendants() {
        List<ProductCategory> descendants = new ArrayList<>();
        for (ProductCategory subCategory : subCategories) {
            descendants.add(subCategory);
            descendants.addAll(subCategory.getAllDescendants());
        }
        return descendants;
    }

    public enum CategoryStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        ARCHIVED
    }

    public enum CategoryType {
        ELECTRONICS,
        AUTOMOTIVE,
        FOOD_AND_BEVERAGE,
        TEXTILE,
        PHARMACEUTICAL,
        MACHINERY,
        CHEMICAL,
        OTHER
    }
}
