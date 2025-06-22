package com.gijun.backend.application.port.out

import com.gijun.backend.domain.product.entities.Product
import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import com.gijun.backend.domain.product.vo.ProductId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.Flow

/**
 * Product Repository 인터페이스
 */
interface ProductRepository {
    
    /**
     * 상품 저장
     */
    suspend fun save(product: Product): Product
    
    /**
     * ID로 상품 조회
     */
    suspend fun findById(productId: ProductId): Product?
    
    /**
     * 상품 삭제
     */
    suspend fun delete(productId: ProductId)
    
    /**
     * 매장의 모든 상품 조회
     */
    fun findByStoreId(storeId: StoreId): Flow<Product>
    
    /**
     * 매장의 활성화된 상품만 조회
     */
    fun findActiveByStoreId(storeId: StoreId): Flow<Product>
    
    /**
     * 매장의 판매 가능한 상품만 조회
     */
    fun findAvailableForSaleByStoreId(storeId: StoreId): Flow<Product>
    
    /**
     * 카테고리별 상품 조회
     */
    fun findByStoreIdAndCategory(storeId: StoreId, category: String): Flow<Product>
    
    /**
     * 상품 유형별 조회
     */
    fun findByStoreIdAndProductType(storeId: StoreId, productType: ProductType): Flow<Product>
    
    /**
     * 상품 상태별 조회
     */
    fun findByStoreIdAndStatus(storeId: StoreId, status: ProductStatus): Flow<Product>
    
    /**
     * 상품명으로 검색
     */
    fun searchByName(storeId: StoreId, searchTerm: String): Flow<Product>
    
    /**
     * 바코드로 상품 조회
     */
    suspend fun findByBarcode(storeId: StoreId, barcode: String): Product?
    
    /**
     * SKU로 상품 조회
     */
    suspend fun findBySku(storeId: StoreId, sku: String): Product?
    
    /**
     * 재고 부족 상품 조회
     */
    fun findLowStockProducts(storeId: StoreId): Flow<Product>
    
    /**
     * 페이징된 상품 목록 조회
     */
    suspend fun findByStoreIdWithPaging(
        storeId: StoreId,
        page: Int,
        size: Int,
        sortBy: String = "displayOrder",
        sortDirection: String = "ASC"
    ): List<Product>
    
    /**
     * 전체 상품 수 조회
     */
    suspend fun countByStoreId(storeId: StoreId): Long
    
    /**
     * 카테고리별 상품 수 조회
     */
    suspend fun countByStoreIdAndCategory(storeId: StoreId, category: String): Long
    
    /**
     * 상품 존재 여부 확인
     */
    suspend fun existsById(productId: ProductId): Boolean
    
    /**
     * 바코드 중복 확인
     */
    suspend fun existsByBarcodeAndStoreId(barcode: String, storeId: StoreId): Boolean
    
    /**
     * SKU 중복 확인
     */
    suspend fun existsBySkuAndStoreId(sku: String, storeId: StoreId): Boolean
}
