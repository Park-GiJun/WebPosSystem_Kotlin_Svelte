package com.gijun.backend.adapter.out.persistence.product

import com.gijun.backend.application.port.out.ProductRepository
import com.gijun.backend.domain.product.entities.Product
import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import com.gijun.backend.domain.product.vo.ProductId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository

/**
 * Product Repository 구현체
 */
@Repository
@Primary
class ProductRepositoryImpl(
    private val productR2dbcRepository: ProductR2dbcRepository,
    private val productMapper: ProductMapper,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) : ProductRepository {

    override suspend fun save(product: Product): Product {
        val entity = productMapper.toEntity(product)
        val savedEntity = productR2dbcRepository.save(entity)
        return productMapper.toDomain(savedEntity)
    }

    /**
     * 새로운 상품 삽입
     */
    suspend fun insert(product: Product): Product {
        val entity = productMapper.toNewEntity(product)
        val savedEntity = productR2dbcRepository.save(entity)
        return productMapper.toDomain(savedEntity)
    }

    /**
     * 기존 상품 업데이트
     */
    suspend fun update(product: Product): Product {
        val entity = productMapper.toEntity(product)
        val savedEntity = productR2dbcRepository.save(entity)
        return productMapper.toDomain(savedEntity)
    }

    override suspend fun findById(productId: ProductId): Product? {
        val entity = productR2dbcRepository.findById(productId.value)
        return entity?.let { productMapper.toDomain(it) }
    }

    override suspend fun delete(productId: ProductId) {
        productR2dbcRepository.deleteById(productId.value)
    }

    override fun findByStoreId(storeId: StoreId): Flow<Product> {
        return productR2dbcRepository.findByStoreIdOrderByDisplayOrder(storeId.value)
            .map { productMapper.toDomain(it) }
    }

    override fun findActiveByStoreId(storeId: StoreId): Flow<Product> {
        return productR2dbcRepository.findByStoreIdAndIsActiveTrueOrderByDisplayOrder(storeId.value)
            .map { productMapper.toDomain(it) }
    }

    override fun findAvailableForSaleByStoreId(storeId: StoreId): Flow<Product> {
        return productR2dbcRepository.findAvailableForSaleByStoreId(storeId.value)
            .map { productMapper.toDomain(it) }
    }

    override fun findByStoreIdAndCategory(storeId: StoreId, category: String): Flow<Product> {
        return productR2dbcRepository.findByStoreIdAndCategoryOrderByDisplayOrder(storeId.value, category)
            .map { productMapper.toDomain(it) }
    }

    override fun findByStoreIdAndProductType(storeId: StoreId, productType: ProductType): Flow<Product> {
        return productR2dbcRepository.findByStoreIdAndProductTypeOrderByDisplayOrder(storeId.value, productType.name)
            .map { productMapper.toDomain(it) }
    }

    override fun findByStoreIdAndStatus(storeId: StoreId, status: ProductStatus): Flow<Product> {
        return productR2dbcRepository.findByStoreIdAndStatusOrderByDisplayOrder(storeId.value, status.name)
            .map { productMapper.toDomain(it) }
    }

    override fun searchByName(storeId: StoreId, searchTerm: String): Flow<Product> {
        return productR2dbcRepository.searchByName(storeId.value, searchTerm)
            .map { productMapper.toDomain(it) }
    }

    override suspend fun findByBarcode(storeId: StoreId, barcode: String): Product? {
        val entity = productR2dbcRepository.findByStoreIdAndBarcode(storeId.value, barcode)
        return entity?.let { productMapper.toDomain(it) }
    }

    override suspend fun findBySku(storeId: StoreId, sku: String): Product? {
        val entity = productR2dbcRepository.findByStoreIdAndSku(storeId.value, sku)
        return entity?.let { productMapper.toDomain(it) }
    }

    override fun findLowStockProducts(storeId: StoreId): Flow<Product> {
        return productR2dbcRepository.findLowStockProducts(storeId.value)
            .map { productMapper.toDomain(it) }
    }

    override suspend fun findByStoreIdWithPaging(
        storeId: StoreId,
        page: Int,
        size: Int,
        sortBy: String,
        sortDirection: String
    ): List<Product> {
        val offset = (page * size).toLong()
        val entities = productR2dbcRepository.findByStoreIdWithPaging(
            storeId.value, 
            size, 
            offset, 
            sortBy, 
            sortDirection
        )
        return productMapper.toDomainList(entities)
    }

    override suspend fun countByStoreId(storeId: StoreId): Long {
        return productR2dbcRepository.countByStoreId(storeId.value)
    }

    override suspend fun countByStoreIdAndCategory(storeId: StoreId, category: String): Long {
        return productR2dbcRepository.countByStoreIdAndCategory(storeId.value, category)
    }

    override suspend fun existsById(productId: ProductId): Boolean {
        return productR2dbcRepository.existsById(productId.value)
    }

    override suspend fun existsByBarcodeAndStoreId(barcode: String, storeId: StoreId): Boolean {
        return productR2dbcRepository.existsByStoreIdAndBarcode(storeId.value, barcode)
    }

    override suspend fun existsBySkuAndStoreId(sku: String, storeId: StoreId): Boolean {
        return productR2dbcRepository.existsByStoreIdAndSku(storeId.value, sku)
    }

    /**
     * 카테고리 목록 조회
     */
    fun findDistinctCategoriesByStoreId(storeId: StoreId): Flow<String> {
        return productR2dbcRepository.findDistinctCategoriesByStoreId(storeId.value)
    }
}
