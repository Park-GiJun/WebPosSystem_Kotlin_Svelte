package com.gijun.backend.adapter.out.persistence.product

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Product R2DBC Repository
 */
@Repository
interface ProductR2dbcRepository : CoroutineCrudRepository<ProductEntity, String> {

    /**
     * 매장 ID로 상품 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND deleted_at IS NULL
        ORDER BY display_order
    """)
    fun findByStoreIdOrderByDisplayOrder(@Param("storeId") storeId: String): Flow<ProductEntity>

    /**
     * 매장의 활성화된 상품만 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND is_active = true 
        AND deleted_at IS NULL
        ORDER BY display_order
    """)
    fun findByStoreIdAndIsActiveTrueOrderByDisplayOrder(@Param("storeId") storeId: String): Flow<ProductEntity>

    /**
     * 매장의 판매 가능한 상품만 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND is_active = true 
        AND status = 'AVAILABLE' 
        AND stock_quantity > 0
        AND deleted_at IS NULL
        ORDER BY display_order
    """)
    fun findAvailableForSaleByStoreId(@Param("storeId") storeId: String): Flow<ProductEntity>

    /**
     * 카테고리별 상품 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND category = :category 
        AND deleted_at IS NULL
        ORDER BY display_order
    """)
    fun findByStoreIdAndCategoryOrderByDisplayOrder(@Param("storeId") storeId: String, @Param("category") category: String): Flow<ProductEntity>

    /**
     * 상품 유형별 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND product_type = :productType 
        AND deleted_at IS NULL
        ORDER BY display_order
    """)
    fun findByStoreIdAndProductTypeOrderByDisplayOrder(@Param("storeId") storeId: String, @Param("productType") productType: String): Flow<ProductEntity>

    /**
     * 상품 상태별 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND status = :status 
        AND deleted_at IS NULL
        ORDER BY display_order
    """)
    fun findByStoreIdAndStatusOrderByDisplayOrder(@Param("storeId") storeId: String, @Param("status") status: String): Flow<ProductEntity>

    /**
     * 상품명으로 검색 (LIKE 검색, 삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND LOWER(name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        AND deleted_at IS NULL
        ORDER BY display_order
    """)
    fun searchByName(@Param("storeId") storeId: String, @Param("searchTerm") searchTerm: String): Flow<ProductEntity>

    /**
     * 바코드로 상품 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND barcode = :barcode 
        AND deleted_at IS NULL
        LIMIT 1
    """)
    suspend fun findByStoreIdAndBarcode(@Param("storeId") storeId: String, @Param("barcode") barcode: String): ProductEntity?

    /**
     * SKU로 상품 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND sku = :sku 
        AND deleted_at IS NULL
        LIMIT 1
    """)
    suspend fun findByStoreIdAndSku(@Param("storeId") storeId: String, @Param("sku") sku: String): ProductEntity?

    /**
     * 재고 부족 상품 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND stock_quantity <= min_stock_level
        AND is_active = true
        AND deleted_at IS NULL
        ORDER BY stock_quantity ASC, display_order
    """)
    fun findLowStockProducts(@Param("storeId") storeId: String): Flow<ProductEntity>

    /**
     * 페이징된 상품 목록 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT * FROM products 
        WHERE store_id = :storeId 
        AND deleted_at IS NULL
        ORDER BY 
        CASE WHEN :sortBy = 'name' AND :sortDirection = 'ASC' THEN name END ASC,
        CASE WHEN :sortBy = 'name' AND :sortDirection = 'DESC' THEN name END DESC,
        CASE WHEN :sortBy = 'price' AND :sortDirection = 'ASC' THEN price END ASC,
        CASE WHEN :sortBy = 'price' AND :sortDirection = 'DESC' THEN price END DESC,
        CASE WHEN :sortBy = 'stockQuantity' AND :sortDirection = 'ASC' THEN stock_quantity END ASC,
        CASE WHEN :sortBy = 'stockQuantity' AND :sortDirection = 'DESC' THEN stock_quantity END DESC,
        CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'ASC' THEN created_at END ASC,
        CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'DESC' THEN created_at END DESC,
        CASE WHEN :sortBy = 'displayOrder' OR :sortBy = '' THEN display_order END ASC
        LIMIT :size OFFSET :offset
    """)
    suspend fun findByStoreIdWithPaging(
        @Param("storeId") storeId: String,
        @Param("size") size: Int,
        @Param("offset") offset: Long,
        @Param("sortBy") sortBy: String,
        @Param("sortDirection") sortDirection: String
    ): List<ProductEntity>

    /**
     * 전체 상품 수 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT COUNT(*) FROM products 
        WHERE store_id = :storeId 
        AND deleted_at IS NULL
    """)
    suspend fun countByStoreId(@Param("storeId") storeId: String): Long

    /**
     * 카테고리별 상품 수 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT COUNT(*) FROM products 
        WHERE store_id = :storeId 
        AND category = :category 
        AND deleted_at IS NULL
    """)
    suspend fun countByStoreIdAndCategory(@Param("storeId") storeId: String, @Param("category") category: String): Long

    /**
     * 바코드 중복 확인 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM products 
        WHERE store_id = :storeId 
        AND barcode = :barcode 
        AND deleted_at IS NULL
    """)
    suspend fun existsByStoreIdAndBarcode(@Param("storeId") storeId: String, @Param("barcode") barcode: String): Boolean

    /**
     * SKU 중복 확인 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM products 
        WHERE store_id = :storeId 
        AND sku = :sku 
        AND deleted_at IS NULL
    """)
    suspend fun existsByStoreIdAndSku(@Param("storeId") storeId: String, @Param("sku") sku: String): Boolean

    /**
     * 카테고리 목록 조회 (삭제되지 않은 것만)
     */
    @Query("""
        SELECT DISTINCT category FROM products 
        WHERE store_id = :storeId 
        AND is_active = true
        AND deleted_at IS NULL
        ORDER BY category
    """)
    fun findDistinctCategoriesByStoreId(@Param("storeId") storeId: String): Flow<String>
}
