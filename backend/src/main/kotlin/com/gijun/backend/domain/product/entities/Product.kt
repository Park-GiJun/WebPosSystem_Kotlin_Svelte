package com.gijun.backend.domain.product.entities

import com.gijun.backend.domain.common.AuditableEntity
import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import com.gijun.backend.domain.product.vo.ProductId
import com.gijun.backend.domain.store.vo.StoreId
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Product 도메인 엔티티
 * 
 * POS 시스템에서 판매되는 상품 정보를 나타내는 도메인 엔티티
 */
data class Product(
    val productId: ProductId,
    val storeId: StoreId,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val originalPrice: BigDecimal,
    val category: String,
    val productType: ProductType,
    val status: ProductStatus,
    val stockQuantity: Int,
    val minStockLevel: Int,
    val maxStockLevel: Int,
    val barcode: String?,
    val sku: String?,
    val imageUrl: String?,
    override val isActive: Boolean,
    val displayOrder: Int,
    override val createdAt: LocalDateTime,
    override val createdBy: String?,
    override val updatedAt: LocalDateTime,
    override val updatedBy: String?,
    override val deletedAt: LocalDateTime? = null,
    override val deletedBy: String? = null
) : AuditableEntity {

    /**
     * 재고가 부족한지 확인
     */
    fun isLowStock(): Boolean {
        return stockQuantity <= minStockLevel
    }

    /**
     * 재고가 충분한지 확인
     */
    fun hasEnoughStock(requestedQuantity: Int): Boolean {
        return stockQuantity >= requestedQuantity
    }

    /**
     * 재고 차감
     */
    fun decreaseStock(quantity: Int): Product {
        require(quantity > 0) { "수량은 0보다 커야 합니다" }
        require(hasEnoughStock(quantity)) { "재고가 부족합니다. 현재 재고: $stockQuantity, 요청 수량: $quantity" }
        
        return copy(stockQuantity = stockQuantity - quantity)
    }

    /**
     * 재고 증가
     */
    fun increaseStock(quantity: Int): Product {
        require(quantity > 0) { "수량은 0보다 커야 합니다" }
        
        val newStockQuantity = stockQuantity + quantity
        require(newStockQuantity <= maxStockLevel) { 
            "최대 재고량을 초과할 수 없습니다. 최대 재고량: $maxStockLevel, 요청 후 재고: $newStockQuantity" 
        }
        
        return copy(stockQuantity = newStockQuantity)
    }

    /**
     * 할인율 계산
     */
    fun getDiscountRate(): BigDecimal {
        if (originalPrice <= BigDecimal.ZERO) return BigDecimal.ZERO
        
        val discount = originalPrice - price
        return (discount.divide(originalPrice, 4, java.math.RoundingMode.HALF_UP))
            .multiply(BigDecimal(100))
    }

    /**
     * 판매 가능한 상품인지 확인
     */
    fun isAvailableForSale(): Boolean {
        return isActive && status == ProductStatus.AVAILABLE && stockQuantity > 0 && !isDeleted()
    }

    /**
     * 상품 정보 업데이트
     */
    fun updateProductInfo(
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
        return copy(
            name = name ?: this.name,
            description = description ?: this.description,
            price = price ?: this.price,
            originalPrice = originalPrice ?: this.originalPrice,
            category = category ?: this.category,
            productType = productType ?: this.productType,
            minStockLevel = minStockLevel ?: this.minStockLevel,
            maxStockLevel = maxStockLevel ?: this.maxStockLevel,
            barcode = barcode ?: this.barcode,
            sku = sku ?: this.sku,
            imageUrl = imageUrl ?: this.imageUrl,
            displayOrder = displayOrder ?: this.displayOrder,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
    }

    /**
     * 상품 상태 변경
     */
    fun updateStatus(status: ProductStatus, updatedBy: String): Product {
        return copy(
            status = status,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
    }

    /**
     * 상품 활성화/비활성화
     */
    fun updateActiveStatus(isActive: Boolean, updatedBy: String): Product {
        return copy(
            isActive = isActive,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
    }

    /**
     * 상품 삭제 (소프트 삭제)
     */
    fun delete(deletedBy: String): Product {
        return copy(
            isActive = false,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy
        )
    }

    companion object {
        /**
         * 새 상품 생성
         */
        fun create(
            productId: ProductId,
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
            require(name.isNotBlank()) { "상품명은 필수입니다" }
            require(price >= BigDecimal.ZERO) { "가격은 0 이상이어야 합니다" }
            require(originalPrice >= BigDecimal.ZERO) { "원래 가격은 0 이상이어야 합니다" }
            require(stockQuantity >= 0) { "재고 수량은 0 이상이어야 합니다" }
            require(minStockLevel >= 0) { "최소 재고량은 0 이상이어야 합니다" }
            require(maxStockLevel >= minStockLevel) { "최대 재고량은 최소 재고량보다 크거나 같아야 합니다" }
            require(category.isNotBlank()) { "카테고리는 필수입니다" }

            val now = LocalDateTime.now()
            return Product(
                productId = productId,
                storeId = storeId,
                name = name,
                description = description,
                price = price,
                originalPrice = originalPrice,
                category = category,
                productType = productType,
                status = ProductStatus.AVAILABLE,
                stockQuantity = stockQuantity,
                minStockLevel = minStockLevel,
                maxStockLevel = maxStockLevel,
                barcode = barcode,
                sku = sku,
                imageUrl = imageUrl,
                isActive = true,
                displayOrder = displayOrder,
                createdAt = now,
                createdBy = createdBy,
                updatedAt = now,
                updatedBy = createdBy
            )
        }
    }
}
