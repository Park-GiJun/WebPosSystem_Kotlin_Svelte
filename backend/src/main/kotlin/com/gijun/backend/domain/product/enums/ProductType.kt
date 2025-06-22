package com.gijun.backend.domain.product.enums

/**
 * 상품 유형 열거형
 */
enum class ProductType(val description: String) {
    /**
     * 음식
     */
    FOOD("음식"),
    
    /**
     * 음료
     */
    BEVERAGE("음료"),
    
    /**
     * 디저트
     */
    DESSERT("디저트"),
    
    /**
     * 상품/굿즈
     */
    MERCHANDISE("상품/굿즈"),
    
    /**
     * 서비스
     */
    SERVICE("서비스"),
    
    /**
     * 기타
     */
    ETC("기타");

    companion object {
        /**
         * 문자열로부터 ProductType 변환
         */
        fun fromString(value: String): ProductType? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
