package com.gijun.backend.domain.store.vo

@JvmInline
value class HeadquartersId(val value: String) {
    init {
        require(value.isNotBlank()) { "본사 ID는 필수입니다." }
        require(value.length <= 10) { "본사 ID는 10자를 초과할 수 없습니다." }
    }

    companion object {
        fun fromString(value: String): HeadquartersId = HeadquartersId(value)

        fun generate(hqCode: String): HeadquartersId {
            require(hqCode.isNotBlank()) { "본사 코드는 필수입니다." }
            return HeadquartersId("HQ${hqCode.uppercase()}")
        }
    }
}
