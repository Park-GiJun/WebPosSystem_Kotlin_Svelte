package com.gijun.backend.adapter.`in`.web.dto.product

import com.gijun.backend.domain.product.entities.Product
import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 상품 응답 DTO
 */
data class ProductResponse(
    val productId: String,
    val storeId: String,
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
    val isActive: Boolean,
    val displayOrder: Int,
    val discountRate: BigDecimal,
    val isLowStock: Boolean,
    val isAvailableForSale: Boolean,
    val createdAt: LocalDateTime,
    val createdBy: String?,
    val updatedAt: LocalDateTime,
    val updatedBy: String?,
    val deletedAt: LocalDateTime?,
    val deletedBy: String?
) {
    companion object {
        fun from(product: Product): ProductResponse {
            return ProductResponse(
                productId = product.productId.value,
                storeId = product.storeId.value,
                name = product.name,
                description = product.description,
                price = product.price,
                originalPrice = product.originalPrice,
                category = product.category,
                productType = product.productType,
                status = product.status,
                stockQuantity = product.stockQuantity,
                minStockLevel = product.minStockLevel,
                maxStockLevel = product.maxStockLevel,
                barcode = product.barcode,
                sku = product.sku,
                imageUrl = product.imageUrl,
                isActive = product.isActive,
                displayOrder = product.displayOrder,
                discountRate = product.getDiscountRate(),
                isLowStock = product.isLowStock(),
                isAvailableForSale = product.isAvailableForSale(),
                createdAt = product.createdAt,
                createdBy = product.createdBy,
                updatedAt = product.updatedAt,
                updatedBy = product.updatedBy,
                deletedAt = product.deletedAt,
                deletedBy = product.deletedBy
            )
        }
    }
}

/**
 * 페이징된 상품 응답 DTO
 */
data class ProductPageResponse(
    val products: List<ProductResponse>,
    val totalCount: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
) {
    companion object {
        fun from(products: List<Product>, totalCount: Long, page: Int, size: Int): ProductPageResponse {
            val totalPages = if (size > 0) ((totalCount + size - 1) / size).toInt() else 0
            return ProductPageResponse(
                products = products.map { ProductResponse.from(it) },
                totalCount = totalCount,
                page = page,
                size = size,
                totalPages = totalPages
            )
        }
    }
}

/**
 * 간단한 상품 응답 DTO (목록용)
 */
data class ProductSummaryResponse(
    val productId: String,
    val name: String,
    val price: BigDecimal,
    val category: String,
    val stockQuantity: Int,
    val isActive: Boolean,
    val isAvailableForSale: Boolean
) {
    companion object {
        fun from(product: Product): ProductSummaryResponse {
            return ProductSummaryResponse(
                productId = product.productId.value,
                name = product.name,
                price = product.price,
                category = product.category,
                stockQuantity = product.stockQuantity,
                isActive = product.isActive,
                isAvailableForSale = product.isAvailableForSale()
            )
        }
    }
}

/**
 * 카테고리 응답 DTO
 */
data class CategoryResponse(
    val category: String,
    val productCount: Long
)
