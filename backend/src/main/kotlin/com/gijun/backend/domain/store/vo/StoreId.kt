package com.gijun.backend.domain.store.vo

@JvmInline
value class StoreId(val value: String) {
    init {
        require(value.isNotBlank()) { "매장 ID는 필수입니다." }
        require(value.length <= 20) { "매장 ID는 20자를 초과할 수 없습니다." }
    }

    companion object {
        fun from(value: String): StoreId = StoreId(value)
        
        fun fromString(value: String): StoreId = StoreId(value)

        fun generateChainStoreId(hqCode: String, regionCode: String, storeNumber: String): StoreId {
            val paddedStoreNumber = storeNumber.padStart(3, '0')
            return StoreId("${hqCode}${regionCode}${paddedStoreNumber}")
        }

        fun generateIndividualStoreId(regionCode: String, storeNumber: String): StoreId {
            val paddedStoreNumber = storeNumber.padStart(3, '0')
            return StoreId("IN${regionCode}${paddedStoreNumber}")
        }
    }
}