package com.gijun.backend.application.service

import com.gijun.backend.application.port.out.ProductRepository
import com.gijun.backend.adapter.out.persistence.product.ProductRepositoryImpl
import com.gijun.backend.domain.product.entities.Product
import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import com.gijun.backend.domain.product.vo.ProductId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Product Service
 */
@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {

    /**
     * 새 상품 생성
     */
    suspend fun createProduct(
        storeId: StoreId,
        name: String,
        description: String?,
        price: BigDecimal,
        originalPrice: BigDecimal,
        category: String,
        productType: ProductType,
        stockQuantity: Int,
        minStockLevel: Int,
        maxStockLevel: Int,
        barcode: String?,
        sku: String?,
        imageUrl: String?,
        displayOrder: Int,
        createdBy: String
    ): Product {
        // 바코드 중복 확인
        if (barcode != null && productRepository.existsByBarcodeAndStoreId(barcode, storeId)) {
            throw IllegalArgumentException("이미 존재하는 바코드입니다: $barcode")
        }

        // SKU 중복 확인
        if (sku != null && productRepository.existsBySkuAndStoreId(sku, storeId)) {
            throw IllegalArgumentException("이미 존재하는 SKU입니다: $sku")
        }

        val productId = ProductId.generate()
        val product = Product.create(
            productId = productId,
            storeId = storeId,
            name = name,
            description = description,
            price = price,
            originalPrice = originalPrice,
            category = category,
            productType = productType,
            stockQuantity = stockQuantity,
            minStockLevel = minStockLevel,
            maxStockLevel = maxStockLevel,
            barcode = barcode,
            sku = sku,
            imageUrl = imageUrl,
            displayOrder = displayOrder,
            createdBy = createdBy
        )

        // ProductRepositoryImpl을 타입 캐스팅해서 insert 메서드 사용
        return if (productRepository is ProductRepositoryImpl) {
            productRepository.insert(product)
        } else {
            productRepository.save(product)
        }
    }

    /**
     * 상품 정보 수정
     */
    suspend fun updateProduct(
        productId: ProductId,
        name: String? = null,
        description: String? = null,
        price: BigDecimal? = null,
        originalPrice: BigDecimal? = null,
        category: String? = null,
        productType: ProductType? = null,
        minStockLevel: Int? = null,
        maxStockLevel: Int? = null,
        barcode: String? = null,
        sku: String? = null,
        imageUrl: String? = null,
        displayOrder: Int? = null,
        updatedBy: String
    ): Product {
        val existingProduct = productRepository.findById(productId)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${productId.value}")

        // 바코드 중복 확인 (기존 값과 다를 때만)
        if (barcode != null && barcode != existingProduct.barcode) {
            if (productRepository.existsByBarcodeAndStoreId(barcode, existingProduct.storeId)) {
                throw IllegalArgumentException("이미 존재하는 바코드입니다: $barcode")
            }
        }

        // SKU 중복 확인 (기존 값과 다를 때만)
        if (sku != null && sku != existingProduct.sku) {
            if (productRepository.existsBySkuAndStoreId(sku, existingProduct.storeId)) {
                throw IllegalArgumentException("이미 존재하는 SKU입니다: $sku")
            }
        }

        val updatedProduct = existingProduct.updateProductInfo(
            name = name,
            description = description,
            price = price,
            originalPrice = originalPrice,
            category = category,
            productType = productType,
            minStockLevel = minStockLevel,
            maxStockLevel = maxStockLevel,
            barcode = barcode,
            sku = sku,
            imageUrl = imageUrl,
            displayOrder = displayOrder,
            updatedBy = updatedBy
        )

        // ProductRepositoryImpl을 타입 캐스팅해서 update 메서드 사용
        return if (productRepository is ProductRepositoryImpl) {
            productRepository.update(updatedProduct)
        } else {
            productRepository.save(updatedProduct)
        }
    }

    /**
     * 상품 상태 변경
     */
    suspend fun updateProductStatus(
        productId: ProductId,
        status: ProductStatus,
        updatedBy: String
    ): Product {
        val existingProduct = productRepository.findById(productId)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${productId.value}")

        val updatedProduct = existingProduct.updateStatus(status, updatedBy)
        // ProductRepositoryImpl을 타입 캐스팅해서 update 메서드 사용
        return if (productRepository is ProductRepositoryImpl) {
            productRepository.update(updatedProduct)
        } else {
            productRepository.save(updatedProduct)
        }
    }

    /**
     * 상품 활성화/비활성화
     */
    suspend fun updateProductActiveStatus(
        productId: ProductId,
        isActive: Boolean,
        updatedBy: String
    ): Product {
        val existingProduct = productRepository.findById(productId)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${productId.value}")

        val updatedProduct = existingProduct.updateActiveStatus(isActive, updatedBy)
        // ProductRepositoryImpl을 타입 캐스팅해서 update 메서드 사용
        return if (productRepository is ProductRepositoryImpl) {
            productRepository.update(updatedProduct)
        } else {
            productRepository.save(updatedProduct)
        }
    }

    /**
     * 재고 조정
     */
    suspend fun adjustStock(
        productId: ProductId,
        quantity: Int,
        updatedBy: String
    ): Product {
        val existingProduct = productRepository.findById(productId)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${productId.value}")

        val updatedProduct = if (quantity > 0) {
            existingProduct.increaseStock(quantity)
        } else {
            existingProduct.decreaseStock(-quantity)
        }

        val finalProduct = updatedProduct.copy(updatedBy = updatedBy)
        
        // ProductRepositoryImpl을 타입 캐스팅해서 update 메서드 사용
        return if (productRepository is ProductRepositoryImpl) {
            productRepository.update(finalProduct)
        } else {
            productRepository.save(finalProduct)
        }
    }

    /**
     * 상품 삭제 (소프트 삭제)
     */
    suspend fun deleteProduct(productId: ProductId, deletedBy: String) {
        val existingProduct = productRepository.findById(productId)
            ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${productId.value}")
        
        val deletedProduct = existingProduct.delete(deletedBy)
        
        // ProductRepositoryImpl을 타입 캐스팅해서 update 메서드 사용
        if (productRepository is ProductRepositoryImpl) {
            productRepository.update(deletedProduct)
        } else {
            productRepository.save(deletedProduct)
        }
    }

    /**
     * 상품 조회
     */
    @Transactional(readOnly = true)
    suspend fun getProduct(productId: ProductId): Product? {
        return productRepository.findById(productId)
    }

    /**
     * 매장의 모든 상품 조회
     */
    @Transactional(readOnly = true)
    fun getProductsByStore(storeId: StoreId): Flow<Product> {
        return productRepository.findByStoreId(storeId)
    }

    /**
     * 매장의 활성화된 상품만 조회
     */
    @Transactional(readOnly = true)
    fun getActiveProductsByStore(storeId: StoreId): Flow<Product> {
        return productRepository.findActiveByStoreId(storeId)
    }

    /**
     * 매장의 판매 가능한 상품만 조회
     */
    @Transactional(readOnly = true)
    fun getAvailableProductsByStore(storeId: StoreId): Flow<Product> {
        return productRepository.findAvailableForSaleByStoreId(storeId)
    }

    /**
     * 카테고리별 상품 조회
     */
    @Transactional(readOnly = true)
    fun getProductsByCategory(storeId: StoreId, category: String): Flow<Product> {
        return productRepository.findByStoreIdAndCategory(storeId, category)
    }

    /**
     * 상품명으로 검색
     */
    @Transactional(readOnly = true)
    fun searchProducts(storeId: StoreId, searchTerm: String): Flow<Product> {
        return productRepository.searchByName(storeId, searchTerm)
    }

    /**
     * 바코드로 상품 조회
     */
    @Transactional(readOnly = true)
    suspend fun getProductByBarcode(storeId: StoreId, barcode: String): Product? {
        return productRepository.findByBarcode(storeId, barcode)
    }

    /**
     * SKU로 상품 조회
     */
    @Transactional(readOnly = true)
    suspend fun getProductBySku(storeId: StoreId, sku: String): Product? {
        return productRepository.findBySku(storeId, sku)
    }

    /**
     * 재고 부족 상품 조회
     */
    @Transactional(readOnly = true)
    fun getLowStockProducts(storeId: StoreId): Flow<Product> {
        return productRepository.findLowStockProducts(storeId)
    }

    /**
     * 페이징된 상품 목록 조회
     */
    @Transactional(readOnly = true)
    suspend fun getProductsWithPaging(
        storeId: StoreId,
        page: Int,
        size: Int,
        sortBy: String = "displayOrder",
        sortDirection: String = "ASC"
    ): Pair<List<Product>, Long> {
        val products = productRepository.findByStoreIdWithPaging(storeId, page, size, sortBy, sortDirection)
        val totalCount = productRepository.countByStoreId(storeId)
        return Pair(products, totalCount)
    }

    /**
     * 일괄 재고 업데이트
     */
    suspend fun bulkUpdateStock(stockUpdates: Map<ProductId, Int>, updatedBy: String): List<Product> {
        val updatedProducts = mutableListOf<Product>()
        
        for ((productId, newStockQuantity) in stockUpdates) {
            val existingProduct = productRepository.findById(productId)
                ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${productId.value}")
            
            val updatedProduct = existingProduct.copy(
                stockQuantity = newStockQuantity,
                updatedBy = updatedBy
            )
            
            // ProductRepositoryImpl을 타입 캐스팅해서 update 메서드 사용
            val savedProduct = if (productRepository is ProductRepositoryImpl) {
                productRepository.update(updatedProduct)
            } else {
                productRepository.save(updatedProduct)
            }
            
            updatedProducts.add(savedProduct)
        }
        
        return updatedProducts
    }

    /**
     * 상품 판매 (재고 차감)
     */
    suspend fun sellProducts(salesItems: Map<ProductId, Int>, updatedBy: String): List<Product> {
        val updatedProducts = mutableListOf<Product>()
        
        // 먼저 모든 상품의 재고를 확인
        for ((productId, quantity) in salesItems) {
            val product = productRepository.findById(productId)
                ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${productId.value}")
            
            if (!product.isAvailableForSale()) {
                throw IllegalArgumentException("판매할 수 없는 상품입니다: ${product.name}")
            }
            
            if (!product.hasEnoughStock(quantity)) {
                throw IllegalArgumentException("재고가 부족합니다: ${product.name} (현재 재고: ${product.stockQuantity}, 요청 수량: $quantity)")
            }
        }
        
        // 재고 차감
        for ((productId, quantity) in salesItems) {
            val product = productRepository.findById(productId)!!
            val updatedProduct = product.decreaseStock(quantity).copy(updatedBy = updatedBy)
            
            // ProductRepositoryImpl을 타입 캐스팅해서 update 메서드 사용
            val savedProduct = if (productRepository is ProductRepositoryImpl) {
                productRepository.update(updatedProduct)
            } else {
                productRepository.save(updatedProduct)
            }
            
            updatedProducts.add(savedProduct)
        }
        
        return updatedProducts
    }
}
