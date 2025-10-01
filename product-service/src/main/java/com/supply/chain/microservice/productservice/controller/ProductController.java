package com.supply.chain.microservice.productservice.controller;

import com.supply.chain.microservice.productservice.dto.*;
import com.supply.chain.microservice.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Product management
 * Provides CRUD operations and search functionality for products
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Product Management", description = "APIs for managing logistics products")
public class ProductController {

    private final ProductService productService;

    /**
     * Get product by ID
     */
    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("Getting product by ID: {}", productId);
        ProductResponse product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Get product by SKU
     */
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a specific product by its SKU")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductResponse> getProductBySku(
            @Parameter(description = "Product SKU") @PathVariable String sku) {
        
        log.info("Getting product by SKU: {}", sku);
        ProductResponse product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    /**
     * Create new product
     */
    @PostMapping
    @Operation(summary = "Create new product", description = "Create a new product in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product data"),
        @ApiResponse(responseCode = "409", description = "Product with SKU already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Product creation request") @Valid @RequestBody ProductCreateRequest request) {
        
        log.info("Creating new product with SKU: {}", request.getSku());
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Update existing product
     */
    @PutMapping("/{productId}")
    @Operation(summary = "Update product", description = "Update an existing product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product data"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "409", description = "SKU already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Product update request") @Valid @RequestBody ProductUpdateRequest request) {
        
        log.info("Updating product with ID: {}", productId);
        ProductResponse product = productService.updateProduct(productId, request);
        return ResponseEntity.ok(product);
    }

    /**
     * Delete product
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product", description = "Soft delete a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete product with active inventory"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("Deleting product with ID: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search products
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products with various criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @Parameter(description = "Search term") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Product type") @RequestParam(required = false) String productType,
            @Parameter(description = "Minimum price") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum weight") @RequestParam(required = false) BigDecimal minWeight,
            @Parameter(description = "Maximum weight") @RequestParam(required = false) BigDecimal maxWeight,
            @Parameter(description = "Featured products only") @RequestParam(required = false) Boolean featured,
            @Parameter(description = "Location") @RequestParam(required = false) String location,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "productName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        log.info("Searching products with term: {}, categoryId: {}", searchTerm, categoryId);
        
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
            .searchTerm(searchTerm)
            .categoryId(categoryId)
            .productType(productType != null ? com.supply.chain.microservice.productservice.entity.Product.ProductType.valueOf(productType) : null)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .minWeight(minWeight)
            .maxWeight(maxWeight)
            .featured(featured)
            .location(location)
            .build();
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.searchProducts(criteria, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieve products belonging to a specific category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Category not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "productName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        log.info("Getting products for category ID: {}", categoryId);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get all active products
     */
    @GetMapping
    @Operation(summary = "Get all active products", description = "Retrieve all active products with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Page<ProductResponse>> getAllActiveProducts(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "productName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {
        
        log.info("Getting all active products");
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductResponse> products = productService.getAllActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Get featured products
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured products", description = "Retrieve featured products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Featured products retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts(
            @Parameter(description = "Maximum number of products") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Getting featured products with limit: {}", limit);
        List<ProductResponse> products = productService.getFeaturedProducts(limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Get product availability
     */
    @GetMapping("/{productId}/availability")
    @Operation(summary = "Get product availability", description = "Check product availability across all locations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability information retrieved"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductAvailabilityResponse> getProductAvailability(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("Getting availability for product ID: {}", productId);
        ProductAvailabilityResponse availability = productService.getProductAvailability(productId);
        return ResponseEntity.ok(availability);
    }

    /**
     * Check product availability for specific quantity and location
     */
    @GetMapping("/{productId}/check-availability")
    @Operation(summary = "Check product availability", description = "Check if product is available for specific quantity and location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Availability check completed"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<Boolean> checkProductAvailability(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Required quantity") @RequestParam BigDecimal quantity,
            @Parameter(description = "Location (optional)") @RequestParam(required = false) String location) {
        
        log.info("Checking availability for product ID: {}, quantity: {}, location: {}", 
                 productId, quantity, location);
        
        boolean available = productService.isProductAvailable(productId, quantity, location);
        return ResponseEntity.ok(available);
    }

    /**
     * Activate product
     */
    @PostMapping("/{productId}/activate")
    @Operation(summary = "Activate product", description = "Activate an inactive product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product activated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Product already active or cannot be activated"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    public ResponseEntity<ProductResponse> activateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("Activating product with ID: {}", productId);
        ProductResponse product = productService.activateProduct(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Deactivate product
     */
    @PostMapping("/{productId}/deactivate")
    @Operation(summary = "Deactivate product", description = "Deactivate an active product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "400", description = "Product not active"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_MANAGE')")
    public ResponseEntity<ProductResponse> deactivateProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("Deactivating product with ID: {}", productId);
        ProductResponse product = productService.deactivateProduct(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Get similar products
     */
    @GetMapping("/{productId}/similar")
    @Operation(summary = "Get similar products", description = "Get products similar to the given product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Similar products retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductResponse>> getSimilarProducts(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Maximum number of similar products") @RequestParam(defaultValue = "5") int limit) {
        
        log.info("Getting similar products for product ID: {} with limit: {}", productId, limit);
        List<ProductResponse> products = productService.getSimilarProducts(productId, limit);
        return ResponseEntity.ok(products);
    }

    /**
     * Get products with low stock
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get products with low stock", description = "Retrieve products that have low stock levels")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAuthority('INVENTORY_READ')")
    public ResponseEntity<List<ProductResponse>> getProductsWithLowStock() {
        log.info("Getting products with low stock");
        List<ProductResponse> products = productService.getProductsWithLowStock();
        return ResponseEntity.ok(products);
    }
}
