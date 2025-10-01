package com.supply.chain.microservice.productservice.repository;

import com.supply.chain.microservice.productservice.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductCategory entities
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    /**
     * Find category by code
     */
    Optional<ProductCategory> findByCategoryCodeIgnoreCase(String categoryCode);

    /**
     * Find root categories (no parent)
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.parentCategory IS NULL AND c.status = 'ACTIVE' ORDER BY c.displayOrder, c.categoryName")
    List<ProductCategory> findRootCategories();

    /**
     * Find child categories
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.parentCategory.id = :parentId AND c.status = 'ACTIVE' ORDER BY c.displayOrder, c.categoryName")
    List<ProductCategory> findChildCategories(@Param("parentId") Long parentId);

    /**
     * Find categories by status
     */
    Page<ProductCategory> findByStatus(ProductCategory.CategoryStatus status, Pageable pageable);

    /**
     * Find categories by level
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.level = :level AND c.status = 'ACTIVE' ORDER BY c.displayOrder, c.categoryName")
    List<ProductCategory> findByLevel(@Param("level") Integer level);

    /**
     * Search categories by name
     */
    @Query("SELECT c FROM ProductCategory c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND c.status = 'ACTIVE'")
    Page<ProductCategory> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find all subcategories of a parent (recursive)
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.hierarchyPath LIKE CONCAT(:parentPath, '%') AND c.status = 'ACTIVE'")
    List<ProductCategory> findAllSubcategories(@Param("parentPath") String parentPath);

    /**
     * Check if category code exists (excluding specific ID)
     */
    @Query("SELECT COUNT(c) > 0 FROM ProductCategory c WHERE c.categoryCode = :categoryCode AND (:excludeId IS NULL OR c.id != :excludeId)")
    boolean existsByCategoryCodeAndIdNot(@Param("categoryCode") String categoryCode, @Param("excludeId") Long excludeId);

    /**
     * Count products in category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.status = 'ACTIVE'")
    long countProductsInCategory(@Param("categoryId") Long categoryId);

    /**
     * Find categories with products
     */
    @Query("SELECT DISTINCT c FROM ProductCategory c JOIN Product p ON p.category.id = c.id WHERE p.status = 'ACTIVE' AND c.status = 'ACTIVE'")
    List<ProductCategory> findCategoriesWithProducts();

    /**
     * Find leaf categories (no children)
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.id NOT IN (SELECT DISTINCT pc.parentCategory.id FROM ProductCategory pc WHERE pc.parentCategory IS NOT NULL) AND c.status = 'ACTIVE'")
    List<ProductCategory> findLeafCategories();

    /**
     * Get category hierarchy for a specific category
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.hierarchyPath LIKE CONCAT('%', :categoryId, '%') ORDER BY c.level")
    List<ProductCategory> getCategoryHierarchy(@Param("categoryId") String categoryId);

    /**
     * Find categories by type
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.categoryType = :categoryType AND c.status = 'ACTIVE' ORDER BY c.displayOrder, c.categoryName")
    List<ProductCategory> findByCategoryType(@Param("categoryType") ProductCategory.CategoryType categoryType);

    /**
     * Find siblings of a category
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.parentCategory.id = :parentId AND c.id != :excludeId AND c.status = 'ACTIVE'")
    List<ProductCategory> findSiblingCategories(@Param("parentId") Long parentId, @Param("excludeId") Long excludeId);

    /**
     * Get max display order for parent
     */
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM ProductCategory c WHERE c.parentCategory.id = :parentId")
    Integer getMaxDisplayOrderForParent(@Param("parentId") Long parentId);

    /**
     * Get max display order for root categories
     */
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM ProductCategory c WHERE c.parentCategory IS NULL")
    Integer getMaxDisplayOrderForRoot();

    /**
     * Find categories requiring approval
     */
    @Query("SELECT c FROM ProductCategory c WHERE c.status IN ('PENDING_APPROVAL', 'UNDER_REVIEW')")
    Page<ProductCategory> findCategoriesRequiringApproval(Pageable pageable);
}
