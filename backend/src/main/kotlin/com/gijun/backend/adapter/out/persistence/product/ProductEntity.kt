package com.gijun.backend.adapter.out.persistence.product

import com.gijun.backend.domain.product.enums.ProductStatus
import com.gijun.backend.domain.product.enums.ProductType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Product 엔티티
 */
@Table("products")
data class ProductEntity(
    @Id
    @Column("product_id")
    val productId: String,
    
    @Column("store_id")
    val storeId: String,
    
    @Column("name")
    val name: String,
    
    @Column("description")
    val description: String?,
    
    @Column("price")
    val price: BigDecimal,
    
    @Column("original_price")
    val originalPrice: BigDecimal,
    
    @Column("category")
    val category: String,
    
    @Column("product_type")
    val productType: String,
    
    @Column("status")
    val status: String,
    
    @Column("stock_quantity")
    val stockQuantity: Int,
    
    @Column("min_stock_level")
    val minStockLevel: Int,
    
    @Column("max_stock_level")
    val maxStockLevel: Int,
    
    @Column("barcode")
    val barcode: String?,
    
    @Column("sku")
    val sku: String?,
    
    @Column("image_url")
    val imageUrl: String?,
    
    @Column("is_active")
    val isActive: Boolean,
    
    @Column("display_order")
    val displayOrder: Int,
    
    @Column("created_at")
    val createdAt: LocalDateTime,
    
    @Column("created_by")
    val createdBy: String?,
    
    @Column("updated_at")
    val updatedAt: LocalDateTime,
    
    @Column("updated_by")
    val updatedBy: String?,
    
    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null,
    
    @Column("deleted_by")
    val deletedBy: String? = null
)
