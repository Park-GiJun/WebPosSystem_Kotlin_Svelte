package com.gijun.backend.domain.product.vo

import java.util.*

/**
 * 상품 ID Value Object
 */
@JvmInline
value class ProductId(val value: String) {
    
    init {
        require(value.isNotBlank()) { "Product ID는 비어있을 수 없습니다" }
    }

    companion object {
        /**
         * 새로운 ProductId 생성
         */
        fun generate(): ProductId {
            return ProductId(UUID.randomUUID().toString())
        }

        /**
         * 문자열로부터 ProductId 생성
         */
        fun from(value: String): ProductId {
            return ProductId(value)
        }
    }
}
