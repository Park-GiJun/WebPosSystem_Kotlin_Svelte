package com.gijun.backend.domain.product.enums

/**
 * 상품 상태 열거형
 */
enum class ProductStatus(val description: String) {
    /**
     * 판매 가능
     */
    AVAILABLE("판매 가능"),
    
    /**
     * 품절
     */
    OUT_OF_STOCK("품절"),
    
    /**
     * 판매 중단
     */
    DISCONTINUED("판매 중단"),
    
    /**
     * 임시 비활성화
     */
    TEMPORARILY_UNAVAILABLE("임시 비활성화"),
    
    /**
     * 준비 중
     */
    PREPARING("준비 중");

    companion object {
        /**
         * 문자열로부터 ProductStatus 변환
         */
        fun fromString(value: String): ProductStatus? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
