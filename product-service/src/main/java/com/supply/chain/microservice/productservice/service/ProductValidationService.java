package com.supply.chain.microservice.productservice.service;

import com.supply.chain.microservice.productservice.dto.ProductCreateRequest;
import com.supply.chain.microservice.productservice.dto.ProductUpdateRequest;
import com.supply.chain.microservice.productservice.exception.InvalidProductDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * Service for product validation logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductValidationService {

    /**
     * Validate product create request
     */
    public void validateCreateRequest(ProductCreateRequest request) {
        log.debug("Validating product create request for SKU: {}", request.getSku());
        
        if (!StringUtils.hasText(request.getProductName())) {
            throw new InvalidProductDataException("Product name cannot be null or empty");
        }
        
        if (request.getCategoryId() == null) {
            throw new InvalidProductDataException("Category ID is required");
        }
        
        if (request.getProductType() == null) {
            throw new InvalidProductDataException("Product type is required");
        }
        
        if (request.getBasePrice() == null || request.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("Base price must be positive");
        }
        
        validateDimensions(request.getWeight(), request.getLength(), request.getWidth(), request.getHeight());
        validateInventoryData(request.getInitialQuantity(), request.getLowStockThreshold());
    }

    /**
     * Validate product update request
     */
    public void validateUpdateRequest(ProductUpdateRequest request) {
        log.debug("Validating product update request");
        
        if (request.getProductName() != null && !StringUtils.hasText(request.getProductName())) {
            throw new InvalidProductDataException("Product name cannot be empty");
        }
        
        validateDimensions(request.getWeight(), request.getLength(), request.getWidth(), request.getHeight());
    }

    /**
     * Validate product dimensions
     */
    private void validateDimensions(BigDecimal weight, BigDecimal length, BigDecimal width, BigDecimal height) {
        if (weight != null && weight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("Weight must be positive");
        }
        
        if (length != null && length.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("Length must be positive");
        }
        
        if (width != null && width.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("Width must be positive");
        }
        
        if (height != null && height.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductDataException("Height must be positive");
        }
    }

    /**
     * Validate inventory data
     */
    private void validateInventoryData(BigDecimal initialQuantity, BigDecimal lowStockThreshold) {
        if (initialQuantity != null && initialQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Initial quantity cannot be negative");
        }
        
        if (lowStockThreshold != null && lowStockThreshold.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidProductDataException("Low stock threshold cannot be negative");
        }
    }
}
