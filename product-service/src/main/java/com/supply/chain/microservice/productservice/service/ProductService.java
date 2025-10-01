package com.supply.chain.microservice.productservice.service;

import com.supply.chain.microservice.productservice.dto.ProductCreateRequest;
import com.supply.chain.microservice.productservice.dto.ProductResponse;
import com.supply.chain.microservice.productservice.dto.ProductSearchCriteria;
import com.supply.chain.microservice.productservice.dto.ProductUpdateRequest;
import com.supply.chain.microservice.productservice.dto.ProductAvailabilityResponse;
import com.supply.chain.microservice.productservice.entity.Product;
import com.supply.chain.microservice.productservice.entity.ProductCategory;
import com.supply.chain.microservice.productservice.entity.ProductInventory;
import com.supply.chain.microservice.productservice.exception.ProductNotFoundException;
import com.supply.chain.microservice.productservice.exception.DuplicateSkuException;
import com.supply.chain.microservice.productservice.exception.InvalidProductDataException;
import com.supply.chain.microservice.productservice.mapper.ProductMapper;
import com.supply.chain.microservice.productservice.repository.ProductRepository;
import com.supply.chain.microservice.productservice.repository.ProductCategoryRepository;
import com.supply.chain.microservice.productservice.repository.ProductInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Product management
 * Handles all business logic related to products
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ProductInventoryRepository inventoryRepository;
    private final ProductMapper productMapper;
    private final ProductEventService productEventService;
    private final ProductValidationService validationService;

    /**
     * Get product by ID with caching
     */
    @Cacheable(value = "products", key = "#productId")
    public ProductResponse getProductById(Long productId) {
        log.debug("Fetching product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        return productMapper.toResponse(product);
    }

    /**
     * Get product by SKU with caching
     */
    @Cacheable(value = "products", key = "#sku")
    public ProductResponse getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);
        
        if (!StringUtils.hasText(sku)) {
            throw new InvalidProductDataException("SKU cannot be null or empty");
        }
        
        Product product = productRepository.findBySkuIgnoreCase(sku)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
        
        return productMapper.toResponse(product);
    }

    /**
     * Create new product
     */
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, allEntries = true)
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());
        
        // Validate request
        validationService.validateCreateRequest(request);
        
        // Check for duplicate SKU
        if (productRepository.existsBySkuAndIdNot(request.getSku(), null)) {
            throw new DuplicateSkuException("Product with SKU " + request.getSku() + " already exists");
        }
        
        // Validate category
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new InvalidProductDataException("Category not found with ID: " + request.getCategoryId()));
        
        if (category.getStatus() != ProductCategory.CategoryStatus.ACTIVE) {
            throw new InvalidProductDataException("Cannot create product under inactive category");
        }
        
        // Create product entity
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setStatus(Product.ProductStatus.PENDING_APPROVAL);
        product.setCreatedAt(LocalDateTime.now());
        
        // Generate next SKU if not provided
        if (!StringUtils.hasText(product.getSku())) {
            product.setSku(generateNextSku(category.getCategoryCode()));
        }
        
        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());
        
        // Publish product created event
        productEventService.publishProductCreatedEvent(savedProduct);
        
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Update existing product
     */
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, key = "#productId")
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        log.info("Updating product with ID: {}", productId);
        
        // Validate request
        validationService.validateUpdateRequest(request);
        
        // Get existing product
        Product existingProduct = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        // Check SKU uniqueness if changed
        if (StringUtils.hasText(request.getSku()) && !request.getSku().equals(existingProduct.getSku())) {
            if (productRepository.existsBySkuAndIdNot(request.getSku(), productId)) {
                throw new DuplicateSkuException("Product with SKU " + request.getSku() + " already exists");
            }
        }
        
        // Update product fields
        Product updatedProduct = productMapper.updateEntity(existingProduct, request);
        updatedProduct.setUpdatedAt(LocalDateTime.now());
        
        // Handle category change
        if (request.getCategoryId() != null && !request.getCategoryId().equals(existingProduct.getCategory().getId())) {
            ProductCategory newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new InvalidProductDataException("Category not found with ID: " + request.getCategoryId()));
            
            if (newCategory.getStatus() != ProductCategory.CategoryStatus.ACTIVE) {
                throw new InvalidProductDataException("Cannot move product to inactive category");
            }
            
            updatedProduct.setCategory(newCategory);
        }
        
        // Save updated product
        Product savedProduct = productRepository.save(updatedProduct);
        log.info("Product updated successfully with ID: {}", savedProduct.getId());
        
        // Publish product updated event
        productEventService.publishProductUpdatedEvent(savedProduct, existingProduct);
        
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Delete product (soft delete)
     */
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, key = "#productId")
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        // Check if product has active inventory
        List<ProductInventory> activeInventory = inventoryRepository.findAvailableInventoryForProduct(productId);
        if (!activeInventory.isEmpty()) {
            throw new InvalidProductDataException("Cannot delete product with active inventory");
        }
        
        // Soft delete
        product.setStatus(Product.ProductStatus.DELETED);
        product.setUpdatedAt(LocalDateTime.now());
        
        productRepository.save(product);
        log.info("Product soft deleted successfully with ID: {}", productId);
        
        // Publish product deleted event
        productEventService.publishProductDeletedEvent(product);
    }

    /**
     * Search products with criteria
     */
    @Cacheable(value = "productSearch", key = "#criteria.hashCode() + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductResponse> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching products with criteria: {}", criteria);
        
        if (criteria.isEmpty()) {
            return getAllActiveProducts(pageable);
        }
        
        // Build specification for complex search
        Specification<Product> specification = buildProductSpecification(criteria);
        
        Page<Product> products = productRepository.findAll(specification, pageable);
        return products.map(productMapper::toResponse);
    }

    /**
     * Get products by category
     */
    @Cacheable(value = "productsByCategory", key = "#categoryId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Fetching products for category ID: {}", categoryId);
        
        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new InvalidProductDataException("Category not found with ID: " + categoryId);
        }
        
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(productMapper::toResponse);
    }

    /**
     * Get products by category code
     */
    @Cacheable(value = "productsByCategory", key = "#categoryCode + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductResponse> getProductsByCategoryCode(String categoryCode, Pageable pageable) {
        log.debug("Fetching products for category code: {}", categoryCode);
        
        Page<Product> products = productRepository.findByCategoryCode(categoryCode, pageable);
        return products.map(productMapper::toResponse);
    }

    /**
     * Get all active products
     */
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        log.debug("Fetching all active products");
        
        Page<Product> products = productRepository.findByStatus(Product.ProductStatus.ACTIVE, pageable);
        return products.map(productMapper::toResponse);
    }

    /**
     * Get featured products
     */
    @Cacheable(value = "featuredProducts")
    public List<ProductResponse> getFeaturedProducts(int limit) {
        log.debug("Fetching featured products with limit: {}", limit);
        
        Pageable pageable = Pageable.ofSize(limit);
        List<Product> products = productRepository.findFeaturedProducts(pageable);
        return products.stream()
            .map(productMapper::toResponse)
            .toList();
    }

    /**
     * Get products with low stock
     */
    public List<ProductResponse> getProductsWithLowStock() {
        log.debug("Fetching products with low stock");
        
        List<Product> products = productRepository.findProductsWithLowStock();
        return products.stream()
            .map(productMapper::toResponse)
            .toList();
    }

    /**
     * Check product availability
     */
    public boolean isProductAvailable(Long productId, BigDecimal requiredQuantity, String location) {
        log.debug("Checking availability for product ID: {}, quantity: {}, location: {}", 
                  productId, requiredQuantity, location);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            return false;
        }
        
        // Check inventory availability
        if (StringUtils.hasText(location)) {
            Optional<ProductInventory> inventory = inventoryRepository
                .findByProductIdAndWarehouseLocationIgnoreCase(productId, location);
            
            return inventory.map(inv -> {
                    Integer availableQty = inv.getAvailableQuantity();
                    if (availableQty == null) return false;
                    return BigDecimal.valueOf(availableQty).compareTo(requiredQuantity) >= 0;
                })
                .orElse(false);
        } else {
            BigDecimal totalAvailable = inventoryRepository.getTotalAvailableQuantityForProduct(productId);
            return totalAvailable != null && totalAvailable.compareTo(requiredQuantity) >= 0;
        }
    }

    /**
     * Get product availability details
     */
    public ProductAvailabilityResponse getProductAvailability(Long productId) {
        log.debug("Getting availability details for product ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        List<ProductInventory> inventories = inventoryRepository.findByProductId(productId);
        BigDecimal totalAvailable = inventoryRepository.getTotalAvailableQuantityForProduct(productId);
        BigDecimal totalReserved = inventoryRepository.getTotalReservedQuantityForProduct(productId);
        
        return ProductAvailabilityResponse.builder()
            .productId(productId)
            .productSku(product.getSku())
            .productName(product.getProductName())
            .status(product.getStatus())
            .totalAvailable(totalAvailable)
            .totalReserved(totalReserved)
            .inventoryLocations(inventories.stream()
                .map(inv -> ProductAvailabilityResponse.ProductLocationAvailability.builder()
                    .location(inv.getWarehouseLocation())
                    .city(inv.getCity())
                    .country(inv.getCountry())
                    .availableQuantity(BigDecimal.valueOf(inv.getAvailableQuantity() != null ? inv.getAvailableQuantity() : 0))
                    .reservedQuantity(BigDecimal.valueOf(inv.getReservedQuantity() != null ? inv.getReservedQuantity() : 0))
                    .lowStockThreshold(BigDecimal.valueOf(inv.getLowStockThreshold() != null ? inv.getLowStockThreshold() : 0))
                    .isLowStock(inv.isLowStock())
                    .build())
                .collect(java.util.stream.Collectors.toList()))
            .build();
    }

    /**
     * Activate product
     */
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, key = "#productId")
    public ProductResponse activateProduct(Long productId) {
        log.info("Activating product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        if (product.getStatus() == Product.ProductStatus.ACTIVE) {
            throw new InvalidProductDataException("Product is already active");
        }
        
        if (product.getStatus() == Product.ProductStatus.DELETED) {
            throw new InvalidProductDataException("Cannot activate deleted product");
        }
        
        product.setStatus(Product.ProductStatus.ACTIVE);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        log.info("Product activated successfully with ID: {}", productId);
        
        // Publish product activated event
        productEventService.publishProductStatusChangedEvent(savedProduct, "ACTIVATED");
        
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Deactivate product
     */
    @Transactional
    @CacheEvict(value = {"products", "productsByCategory"}, key = "#productId")
    public ProductResponse deactivateProduct(Long productId) {
        log.info("Deactivating product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new InvalidProductDataException("Can only deactivate active products");
        }
        
        product.setStatus(Product.ProductStatus.INACTIVE);
        product.setUpdatedAt(LocalDateTime.now());
        
        Product savedProduct = productRepository.save(product);
        log.info("Product deactivated successfully with ID: {}", productId);
        
        // Publish product deactivated event
        productEventService.publishProductStatusChangedEvent(savedProduct, "DEACTIVATED");
        
        return productMapper.toResponse(savedProduct);
    }

    /**
     * Get similar products
     */
    @Cacheable(value = "similarProducts", key = "#productId + '_' + #limit")
    public List<ProductResponse> getSimilarProducts(Long productId, int limit) {
        log.debug("Finding similar products for product ID: {}", productId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        Pageable pageable = Pageable.ofSize(limit);
        List<Product> similarProducts = productRepository.findSimilarProducts(
            product.getCategory().getId(), productId, pageable);
        
        return similarProducts.stream()
            .map(productMapper::toResponse)
            .toList();
    }

    /**
     * Generate next SKU for category
     */
    private String generateNextSku(String categoryCode) {
        String skuPattern = categoryCode + "%";
        List<Product> existingProducts = productRepository.findBySkuPattern(skuPattern);
        
        int nextNumber = existingProducts.size() + 1;
        return String.format("%s-%04d", categoryCode, nextNumber);
    }

    /**
     * Build product specification for complex search
     */
    private Specification<Product> buildProductSpecification(ProductSearchCriteria criteria) {
        Specification<Product> spec = Specification.where(null);
        
        // Add status filter (always filter for active products unless specified)
        if (criteria.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), criteria.getStatus()));
        } else {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), Product.ProductStatus.ACTIVE));
        }
        
        // Add search term filter
        if (StringUtils.hasText(criteria.getSearchTerm())) {
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("productName")), "%" + criteria.getSearchTerm().toLowerCase() + "%"),
                cb.like(cb.lower(root.get("description")), "%" + criteria.getSearchTerm().toLowerCase() + "%"),
                cb.like(cb.lower(root.get("sku")), "%" + criteria.getSearchTerm().toLowerCase() + "%")
            ));
        }
        
        // Add category filter
        if (criteria.getCategoryId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
        }
        
        // Add product type filter
        if (criteria.getProductType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("productType"), criteria.getProductType()));
        }
        
        // Add price range filter
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            spec = spec.and((root, query, cb) -> {
                var priceJoin = root.join("pricing");
                if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
                    return cb.between(priceJoin.get("basePrice"), criteria.getMinPrice(), criteria.getMaxPrice());
                } else if (criteria.getMinPrice() != null) {
                    return cb.greaterThanOrEqualTo(priceJoin.get("basePrice"), criteria.getMinPrice());
                } else {
                    return cb.lessThanOrEqualTo(priceJoin.get("basePrice"), criteria.getMaxPrice());
                }
            });
        }
        
        // Add weight range filter
        if (criteria.getMinWeight() != null || criteria.getMaxWeight() != null) {
            spec = spec.and((root, query, cb) -> {
                if (criteria.getMinWeight() != null && criteria.getMaxWeight() != null) {
                    return cb.between(root.get("weight"), criteria.getMinWeight(), criteria.getMaxWeight());
                } else if (criteria.getMinWeight() != null) {
                    return cb.greaterThanOrEqualTo(root.get("weight"), criteria.getMinWeight());
                } else {
                    return cb.lessThanOrEqualTo(root.get("weight"), criteria.getMaxWeight());
                }
            });
        }
        
        // Add featured filter
        if (criteria.getFeatured() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("featured"), criteria.getFeatured()));
        }
        
        return spec;
    }
}
