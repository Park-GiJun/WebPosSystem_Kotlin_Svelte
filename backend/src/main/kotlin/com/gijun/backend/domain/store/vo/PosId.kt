package com.gijun.backend.domain.store.vo

@JvmInline
value class PosId(val value: String) {
    init {
        require(value.isNotBlank()) { "POS ID는 필수입니다." }
        require(value.length <= 20) { "POS ID는 20자를 초과할 수 없습니다." }
    }

    companion object {
        fun fromString(value: String): PosId = PosId(value)

        fun generate(storeId: StoreId, posNumber: Int): PosId {
            require(posNumber > 0) { "POS 번호는 1 이상이어야 합니다." }
            val paddedPosNumber = posNumber.toString().padStart(2, '0')
            return PosId("${storeId.value}P${paddedPosNumber}")
        }
    }
}
