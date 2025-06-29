package com.gijun.backend.adapter.`in`.web

import com.gijun.backend.adapter.`in`.web.dto.common.ApiResponse
import com.gijun.backend.application.port.out.StoreRepository
import com.gijun.backend.application.port.out.ProductRepository
import com.gijun.backend.adapter.out.persistence.product.ProductRepositoryImpl
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.vo.StoreId
import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.PhoneNumber
import com.gijun.backend.domain.product.entities.Product
import com.gijun.backend.domain.product.enums.ProductType
import com.gijun.backend.domain.product.vo.ProductId
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 샘플 데이터 생성 컨트롤러 (개발/테스트 용도)
 * 권한 체크 없이 누구나 사용할 수 있습니다.
 */
@RestController
@RequestMapping("/api/v1/sample")
class SampleDataController(
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository
) {

    /**
     * 샘플 매장 생성
     */
    @PostMapping("/stores")
    suspend fun createSampleStores(): ApiResponse<Unit> {
        return try {
            // 샘플 개인 매장 생성
            val sampleStore = Store.createIndividualStore(
                storeName = "카페 테스트",
                regionCode = "001",
                storeNumber = "001",
                ownerName = "김사장",
                createdBy = "system",
                businessLicense = BusinessLicense("123-45-67890"),
                phoneNumber = PhoneNumber("02-1234-5678"),
                address = "서울특별시 강남구 테헤란로 123",
                postalCode = "06142",
                openingDate = LocalDate.now().minusMonths(6)
            )

            storeRepository.save(sampleStore)
            
            ApiResponse.success("샘플 매장이 생성되었습니다. 매장 ID: ${sampleStore.storeId.value}")
        } catch (e: Exception) {
            println("샘플 매장 생성 실패: ${e.message}")
            ApiResponse.success("샘플 매장 생성 실패: ${e.message}")
        }
    }

    /**
     * 샘플 상품 생성
     */
    @PostMapping("/stores/{storeId}/products")
    suspend fun createSampleProducts(@PathVariable storeId: String): ApiResponse<Unit> {
        return try {
            val targetStoreId = StoreId.from(storeId)
            
            // 샘플 상품들 생성
            val sampleProducts = listOf(
                Product.create(
                    productId = ProductId.generate(),
                    storeId = targetStoreId,
                    name = "아메리카노",
                    description = "진한 원두의 깔끔한 맛",
                    price = BigDecimal("4500"),
                    originalPrice = BigDecimal("5000"),
                    category = "BEVERAGE",
                    productType = ProductType.BEVERAGE,
                    stockQuantity = 100,
                    minStockLevel = 10,
                    maxStockLevel = 500,
                    barcode = "8801234567890",
                    sku = "AME-001",
                    imageUrl = null,
                    displayOrder = 1,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = targetStoreId,
                    name = "카페라떼",
                    description = "부드러운 우유와 진한 에스프레소",
                    price = BigDecimal("5000"),
                    originalPrice = BigDecimal("5500"),
                    category = "BEVERAGE",
                    productType = ProductType.BEVERAGE,
                    stockQuantity = 80,
                    minStockLevel = 15,
                    maxStockLevel = 300,
                    barcode = "8801234567891",
                    sku = "LAT-001",
                    imageUrl = null,
                    displayOrder = 2,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = targetStoreId,
                    name = "크로와상",
                    description = "바삭한 프랑스식 페이스트리",
                    price = BigDecimal("3500"),
                    originalPrice = BigDecimal("3500"),
                    category = "FOOD",
                    productType = ProductType.FOOD,
                    stockQuantity = 25,
                    minStockLevel = 5,
                    maxStockLevel = 50,
                    barcode = "8801234567892",
                    sku = "CRO-001",
                    imageUrl = null,
                    displayOrder = 3,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = targetStoreId,
                    name = "치즈케이크",
                    description = "부드러운 크림치즈 케이크",
                    price = BigDecimal("6000"),
                    originalPrice = BigDecimal("6000"),
                    category = "DESSERT",
                    productType = ProductType.DESSERT,
                    stockQuantity = 12,
                    minStockLevel = 3,
                    maxStockLevel = 30,
                    barcode = "8801234567893",
                    sku = "CHE-001",
                    imageUrl = null,
                    displayOrder = 4,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = targetStoreId,
                    name = "텀블러",
                    description = "카페 로고가 새겨진 보온 텀블러",
                    price = BigDecimal("15000"),
                    originalPrice = BigDecimal("18000"),
                    category = "MERCHANDISE",
                    productType = ProductType.MERCHANDISE,
                    stockQuantity = 5,
                    minStockLevel = 2,
                    maxStockLevel = 20,
                    barcode = "8801234567894",
                    sku = "TUM-001",
                    imageUrl = null,
                    displayOrder = 5,
                    createdBy = "system"
                )
            )

            // 상품들 저장
            sampleProducts.forEach { product ->
                if (productRepository is ProductRepositoryImpl) {
                    productRepository.insert(product)
                } else {
                    productRepository.save(product)
                }
            }
            
            ApiResponse.success("${sampleProducts.size}개의 샘플 상품이 생성되었습니다.")
        } catch (e: Exception) {
            println("샘플 상품 생성 실패: ${e.message}")
            e.printStackTrace()
            ApiResponse.success("샘플 상품 생성 실패: ${e.message}")
        }
    }

    /**
     * 샘플 데이터 모두 생성 (매장 + 상품)
     */
    @PostMapping("/all")
    suspend fun createAllSampleData(): Mono<ApiResponse<Map<String, String>>> {
        return try {
            // 1. 샘플 매장 생성
            val sampleStore = Store.createIndividualStore(
                storeName = "카페 테스트",
                regionCode = "001",
                storeNumber = "001",
                ownerName = "김사장",
                createdBy = "system",
                businessLicense = BusinessLicense("123-45-67890"),
                phoneNumber = PhoneNumber("02-1234-5678"),
                address = "서울특별시 강남구 테헤란로 123",
                postalCode = "06142",
                openingDate = LocalDate.now().minusMonths(6)
            )

            val savedStore = storeRepository.save(sampleStore)
            println("샘플 매장 생성 완료: ${savedStore.storeId.value}")

            // 2. 샘플 상품들 생성
            val sampleProducts = listOf(
                Product.create(
                    productId = ProductId.generate(),
                    storeId = savedStore.storeId,
                    name = "아메리카노",
                    description = "진한 원두의 깔끔한 맛",
                    price = BigDecimal("4500"),
                    originalPrice = BigDecimal("5000"),
                    category = "BEVERAGE",
                    productType = ProductType.BEVERAGE,
                    stockQuantity = 100,
                    minStockLevel = 10,
                    maxStockLevel = 500,
                    barcode = "8801234567890",
                    sku = "AME-001",
                    imageUrl = null,
                    displayOrder = 1,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = savedStore.storeId,
                    name = "카페라떼",
                    description = "부드러운 우유와 진한 에스프레소",
                    price = BigDecimal("5000"),
                    originalPrice = BigDecimal("5500"),
                    category = "BEVERAGE",
                    productType = ProductType.BEVERAGE,
                    stockQuantity = 80,
                    minStockLevel = 15,
                    maxStockLevel = 300,
                    barcode = "8801234567891",
                    sku = "LAT-001",
                    imageUrl = null,
                    displayOrder = 2,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = savedStore.storeId,
                    name = "크로와상",
                    description = "바삭한 프랑스식 페이스트리",
                    price = BigDecimal("3500"),
                    originalPrice = BigDecimal("3500"),
                    category = "FOOD",
                    productType = ProductType.FOOD,
                    stockQuantity = 25,
                    minStockLevel = 5,
                    maxStockLevel = 50,
                    barcode = "8801234567892",
                    sku = "CRO-001",
                    imageUrl = null,
                    displayOrder = 3,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = savedStore.storeId,
                    name = "치즈케이크",
                    description = "부드러운 크림치즈 케이크",
                    price = BigDecimal("6000"),
                    originalPrice = BigDecimal("6000"),
                    category = "DESSERT",
                    productType = ProductType.DESSERT,
                    stockQuantity = 12,
                    minStockLevel = 3,
                    maxStockLevel = 30,
                    barcode = "8801234567893",
                    sku = "CHE-001",
                    imageUrl = null,
                    displayOrder = 4,
                    createdBy = "system"
                ),
                Product.create(
                    productId = ProductId.generate(),
                    storeId = savedStore.storeId,
                    name = "텀블러",
                    description = "카페 로고가 새겨진 보온 텀블러",
                    price = BigDecimal("15000"),
                    originalPrice = BigDecimal("18000"),
                    category = "MERCHANDISE",
                    productType = ProductType.MERCHANDISE,
                    stockQuantity = 5,
                    minStockLevel = 2,
                    maxStockLevel = 20,
                    barcode = "8801234567894",
                    sku = "TUM-001",
                    imageUrl = null,
                    displayOrder = 5,
                    createdBy = "system"
                )
            )

            // 상품들 저장
            sampleProducts.forEach { product ->
                if (productRepository is ProductRepositoryImpl) {
                    productRepository.insert(product)
                } else {
                    productRepository.save(product)
                }
                println("샘플 상품 생성 완료: ${product.name}")
            }
            
            val result = mapOf(
                "storeId" to savedStore.storeId.value,
                "storeName" to savedStore.storeName,
                "productCount" to sampleProducts.size.toString(),
                "message" to "샘플 매장과 ${sampleProducts.size}개의 상품이 생성되었습니다."
            )
            
            Mono.just(ApiResponse.success(result))
        } catch (e: Exception) {
            println("샘플 데이터 생성 실패: ${e.message}")
            e.printStackTrace()
            val errorResult = mapOf(
                "error" to "샘플 데이터 생성 실패: ${e.message}"
            )
            Mono.just(ApiResponse.success(errorResult))
        }
    }
}
