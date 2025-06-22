package com.gijun.backend.adapter.out.persistence.product

import com.gijun.backend.domain.product.entities.Product
import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import com.gijun.backend.domain.product.vo.ProductId
import com.gijun.backend.domain.store.vo.StoreId
import org.springframework.stereotype.Component

/**
 * Product 도메인과 엔티티 간 매핑
 */
@Component
class ProductMapper {

    /**
     * 도메인을 엔티티로 변환
     */
    fun toEntity(product: Product): ProductEntity {
        return ProductEntity(
            productId = product.productId.value,
            storeId = product.storeId.value,
            name = product.name,
            description = product.description,
            price = product.price,
            originalPrice = product.originalPrice,
            category = product.category,
            productType = product.productType.name,
            status = product.status.name,
            stockQuantity = product.stockQuantity,
            minStockLevel = product.minStockLevel,
            maxStockLevel = product.maxStockLevel,
            barcode = product.barcode,
            sku = product.sku,
            imageUrl = product.imageUrl,
            isActive = product.isActive,
            displayOrder = product.displayOrder,
            createdAt = product.createdAt,
            createdBy = product.createdBy,
            updatedAt = product.updatedAt,
            updatedBy = product.updatedBy,
            deletedAt = product.deletedAt,
            deletedBy = product.deletedBy
        )
    }

    /**
     * 엔티티를 도메인으로 변환
     */
    fun toDomain(entity: ProductEntity): Product {
        return Product(
            productId = ProductId.from(entity.productId),
            storeId = StoreId.from(entity.storeId),
            name = entity.name,
            description = entity.description,
            price = entity.price,
            originalPrice = entity.originalPrice,
            category = entity.category,
            productType = ProductType.valueOf(entity.productType),
            status = ProductStatus.valueOf(entity.status),
            stockQuantity = entity.stockQuantity,
            minStockLevel = entity.minStockLevel,
            maxStockLevel = entity.maxStockLevel,
            barcode = entity.barcode,
            sku = entity.sku,
            imageUrl = entity.imageUrl,
            isActive = entity.isActive,
            displayOrder = entity.displayOrder,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
            deletedAt = entity.deletedAt,
            deletedBy = entity.deletedBy
        )
    }

    /**
     * 엔티티 리스트를 도메인 리스트로 변환
     */
    fun toDomainList(entities: List<ProductEntity>): List<Product> {
        return entities.map { toDomain(it) }
    }
}
