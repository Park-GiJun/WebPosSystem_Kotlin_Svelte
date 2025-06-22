package com.gijun.backend.adapter.`in`.web.dto.product

import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import java.math.BigDecimal

/**
 * 상품 생성 요청 DTO
 */
data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val originalPrice: BigDecimal,
    val category: String,
    val productType: ProductType,
    val stockQuantity: Int,
    val minStockLevel: Int,
    val maxStockLevel: Int,
    val barcode: String?,
    val sku: String?,
    val imageUrl: String?,
    val displayOrder: Int = 0
)

/**
 * 상품 수정 요청 DTO
 */
data class UpdateProductRequest(
    val name: String?,
    val description: String?,
    val price: BigDecimal?,
    val originalPrice: BigDecimal?,
    val category: String?,
    val productType: ProductType?,
    val minStockLevel: Int?,
    val maxStockLevel: Int?,
    val barcode: String?,
    val sku: String?,
    val imageUrl: String?,
    val displayOrder: Int?
)

/**
 * 상품 상태 변경 요청 DTO
 */
data class UpdateProductStatusRequest(
    val status: ProductStatus
)

/**
 * 상품 활성화 상태 변경 요청 DTO
 */
data class UpdateProductActiveStatusRequest(
    val isActive: Boolean
)

/**
 * 재고 조정 요청 DTO
 */
data class AdjustStockRequest(
    val quantity: Int
)

/**
 * 일괄 재고 업데이트 요청 DTO
 */
data class BulkStockUpdateRequest(
    val stockUpdates: Map<String, Int>
)

/**
 * 상품 판매 요청 DTO
 */
data class SellProductsRequest(
    val salesItems: Map<String, Int>
)

/**
 * 상품 검색 요청 DTO
 */
data class ProductSearchRequest(
    val searchTerm: String?,
    val category: String?,
    val productType: ProductType?,
    val status: ProductStatus?,
    val isActive: Boolean?,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "displayOrder",
    val sortDirection: String = "ASC"
)
