package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.common.ApiResponse
import com.gijun.backend.adapter.`in`.web.dto.product.*
import com.gijun.backend.application.service.ProductService
import com.gijun.backend.configuration.RequiresPermission
import com.gijun.backend.domain.product.vo.ProductId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.toList
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Product Controller
 * 상품 관리 API
 */
@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService
) {

    /**
     * 상품 생성
     */
    @PostMapping("/stores/{storeId}")
    @RequiresPermission("PRODUCT_CREATE")
    suspend fun createProduct(
        @PathVariable storeId: String,
        @RequestBody request: CreateProductRequest,
        authentication: Authentication
    ): ApiResponse<ProductResponse> {
        val username = authentication.name
        
        val product = productService.createProduct(
            storeId = StoreId.from(storeId),
            name = request.name,
            description = request.description,
            price = request.price,
            originalPrice = request.originalPrice,
            category = request.category,
            productType = request.productType,
            stockQuantity = request.stockQuantity,
            minStockLevel = request.minStockLevel,
            maxStockLevel = request.maxStockLevel,
            barcode = request.barcode,
            sku = request.sku,
            imageUrl = request.imageUrl,
            displayOrder = request.displayOrder,
            createdBy = username
        )

        return ApiResponse.success(ProductResponse.from(product))
    }

    /**
     * 상품 수정
     */
    @PutMapping("/{productId}")
    @RequiresPermission("PRODUCT_UPDATE")
    suspend fun updateProduct(
        @PathVariable productId: String,
        @RequestBody request: UpdateProductRequest,
        authentication: Authentication
    ): ApiResponse<ProductResponse> {
        val username = authentication.name
        
        val product = productService.updateProduct(
            productId = ProductId.from(productId),
            name = request.name,
            description = request.description,
            price = request.price,
            originalPrice = request.originalPrice,
            category = request.category,
            productType = request.productType,
            minStockLevel = request.minStockLevel,
            maxStockLevel = request.maxStockLevel,
            barcode = request.barcode,
            sku = request.sku,
            imageUrl = request.imageUrl,
            displayOrder = request.displayOrder,
            updatedBy = username
        )

        return ApiResponse.success(ProductResponse.from(product))
    }

    /**
     * 상품 상태 변경
     */
    @PatchMapping("/{productId}/status")
    @RequiresPermission("PRODUCT_UPDATE")
    suspend fun updateProductStatus(
        @PathVariable productId: String,
        @RequestBody request: UpdateProductStatusRequest,
        authentication: Authentication
    ): ApiResponse<ProductResponse> {
        val username = authentication.name
        
        val product = productService.updateProductStatus(
            productId = ProductId.from(productId),
            status = request.status,
            updatedBy = username
        )

        return ApiResponse.success(ProductResponse.from(product))
    }

    /**
     * 상품 활성화/비활성화
     */
    @PatchMapping("/{productId}/active")
    @RequiresPermission("PRODUCT_UPDATE")
    suspend fun updateProductActiveStatus(
        @PathVariable productId: String,
        @RequestBody request: UpdateProductActiveStatusRequest,
        authentication: Authentication
    ): ApiResponse<ProductResponse> {
        val username = authentication.name
        
        val product = productService.updateProductActiveStatus(
            productId = ProductId.from(productId),
            isActive = request.isActive,
            updatedBy = username
        )

        return ApiResponse.success(ProductResponse.from(product))
    }

    /**
     * 재고 조정
     */
    @PatchMapping("/{productId}/stock")
    @RequiresPermission("PRODUCT_STOCK_MANAGE")
    suspend fun adjustStock(
        @PathVariable productId: String,
        @RequestBody request: AdjustStockRequest,
        authentication: Authentication
    ): ApiResponse<ProductResponse> {
        val username = authentication.name
        
        val product = productService.adjustStock(
            productId = ProductId.from(productId),
            quantity = request.quantity,
            updatedBy = username
        )

        return ApiResponse.success(ProductResponse.from(product))
    }

    /**
     * 상품 삭제 (소프트 삭제)
     */
    @DeleteMapping("/{productId}")
    @RequiresPermission("PRODUCT_DELETE")
    suspend fun deleteProduct(
        @PathVariable productId: String,
        authentication: Authentication
    ): ApiResponse<Unit> {
        val username = authentication.name
        productService.deleteProduct(ProductId.from(productId), username)
        return ApiResponse.success()
    }

    /**
     * 상품 상세 조회
     */
    @GetMapping("/{productId}")
    @RequiresPermission("PRODUCT_READ")
    suspend fun getProduct(
        @PathVariable productId: String
    ): ApiResponse<ProductResponse?> {
        val product = productService.getProduct(ProductId.from(productId))
        val response = product?.let { ProductResponse.from(it) }
        return ApiResponse.success(response)
    }

    /**
     * 매장의 모든 상품 조회
     */
    @GetMapping("/stores/{storeId}")
    @RequiresPermission("PRODUCT_READ")
    suspend fun getProductsByStore(
        @PathVariable storeId: String,
        @RequestParam(defaultValue = "false") activeOnly: Boolean,
        @RequestParam(defaultValue = "false") availableOnly: Boolean
    ): ApiResponse<List<ProductResponse>> {
        return try {
            val products = when {
                availableOnly -> productService.getAvailableProductsByStore(StoreId.from(storeId))
                activeOnly -> productService.getActiveProductsByStore(StoreId.from(storeId))
                else -> productService.getProductsByStore(StoreId.from(storeId))
            }.toList()

            val response = products.map { ProductResponse.from(it) }
            ApiResponse.success(response)
        } catch (e: Exception) {
            println("상품 조회 중 오류 발생: ${e.message}")
            ApiResponse.success(emptyList<ProductResponse>())
        }
    }

    /**
     * 페이징된 상품 목록 조회
     */
    @GetMapping("/stores/{storeId}/paged")
    @RequiresPermission("PRODUCT_READ")
    suspend fun getProductsWithPaging(
        @PathVariable storeId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "displayOrder") sortBy: String,
        @RequestParam(defaultValue = "ASC") sortDirection: String
    ): ApiResponse<ProductPageResponse> {
        return try {
            val (products, totalCount) = productService.getProductsWithPaging(
                storeId = StoreId.from(storeId),
                page = page,
                size = size,
                sortBy = sortBy,
                sortDirection = sortDirection
            )

            val response = ProductPageResponse.from(products, totalCount, page, size)
            ApiResponse.success(response)
        } catch (e: Exception) {
            println("페이징된 상품 조회 중 오류 발생: ${e.message}")
            val emptyResponse = ProductPageResponse.from(emptyList(), 0, page, size)
            ApiResponse.success(emptyResponse)
        }
    }

    /**
     * 카테고리별 상품 조회
     */
    @GetMapping("/stores/{storeId}/categories/{category}")
    @RequiresPermission("PRODUCT_READ")
    suspend fun getProductsByCategory(
        @PathVariable storeId: String,
        @PathVariable category: String
    ): ApiResponse<List<ProductResponse>> {
        val products = productService.getProductsByCategory(
            StoreId.from(storeId), 
            category
        ).toList()

        val response = products.map { ProductResponse.from(it) }
        return ApiResponse.success(response)
    }

    /**
     * 상품 검색
     */
    @GetMapping("/stores/{storeId}/search")
    @RequiresPermission("PRODUCT_READ")
    suspend fun searchProducts(
        @PathVariable storeId: String,
        @RequestParam searchTerm: String
    ): ApiResponse<List<ProductResponse>> {
        val products = productService.searchProducts(
            StoreId.from(storeId), 
            searchTerm
        ).toList()

        val response = products.map { ProductResponse.from(it) }
        return ApiResponse.success(response)
    }

    /**
     * 바코드로 상품 조회
     */
    @GetMapping("/stores/{storeId}/barcode/{barcode}")
    @RequiresPermission("PRODUCT_READ")
    suspend fun getProductByBarcode(
        @PathVariable storeId: String,
        @PathVariable barcode: String
    ): ApiResponse<ProductResponse?> {
        val product = productService.getProductByBarcode(StoreId.from(storeId), barcode)
        val response = product?.let { ProductResponse.from(it) }
        return ApiResponse.success(response)
    }

    /**
     * SKU로 상품 조회
     */
    @GetMapping("/stores/{storeId}/sku/{sku}")
    @RequiresPermission("PRODUCT_READ")
    suspend fun getProductBySku(
        @PathVariable storeId: String,
        @PathVariable sku: String
    ): ApiResponse<ProductResponse?> {
        val product = productService.getProductBySku(StoreId.from(storeId), sku)
        val response = product?.let { ProductResponse.from(it) }
        return ApiResponse.success(response)
    }

    /**
     * 재고 부족 상품 조회
     */
    @GetMapping("/stores/{storeId}/low-stock")
    @RequiresPermission("PRODUCT_READ")
    suspend fun getLowStockProducts(
        @PathVariable storeId: String
    ): ApiResponse<List<ProductResponse>> {
        val products = productService.getLowStockProducts(StoreId.from(storeId)).toList()
        val response = products.map { ProductResponse.from(it) }
        return ApiResponse.success(response)
    }

    /**
     * 일괄 재고 업데이트
     */
    @PutMapping("/bulk-stock")
    @RequiresPermission("PRODUCT_STOCK_MANAGE")
    suspend fun bulkUpdateStock(
        @RequestBody request: BulkStockUpdateRequest,
        authentication: Authentication
    ): ApiResponse<List<ProductResponse>> {
        val username = authentication.name
        
        val stockUpdates = request.stockUpdates.mapKeys { ProductId.from(it.key) }
        val products = productService.bulkUpdateStock(stockUpdates, username)
        val response = products.map { ProductResponse.from(it) }
        
        return ApiResponse.success(response)
    }

    /**
     * 상품 판매 (재고 차감)
     */
    @PostMapping("/sell")
    @RequiresPermission("PRODUCT_SELL")
    suspend fun sellProducts(
        @RequestBody request: SellProductsRequest,
        authentication: Authentication
    ): ApiResponse<List<ProductResponse>> {
        val username = authentication.name
        
        val salesItems = request.salesItems.mapKeys { ProductId.from(it.key) }
        val products = productService.sellProducts(salesItems, username)
        val response = products.map { ProductResponse.from(it) }
        
        return ApiResponse.success(response)
    }

    /**
     * 매장의 판매 가능한 상품 요약 조회 (POS용)
     */
    @GetMapping("/stores/{storeId}/pos")
    @RequiresPermission("POS_SALES")
    suspend fun getProductsForPos(
        @PathVariable storeId: String
    ): ApiResponse<List<ProductSummaryResponse>> {
        return try {
            val products = productService.getAvailableProductsByStore(StoreId.from(storeId)).toList()
            val response = products.map { ProductSummaryResponse.from(it) }
            ApiResponse.success(response)
        } catch (e: Exception) {
            println("POS용 상품 조회 중 오류 발생: ${e.message}")
            ApiResponse.success(emptyList<ProductSummaryResponse>())
        }
    }
}
