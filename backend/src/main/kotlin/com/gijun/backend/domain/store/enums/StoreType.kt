package com.gijun.backend.domain.store.enums

enum class StoreType(val description: String) {
    CHAIN("체인점"),
    INDIVIDUAL("개인매장");

    fun isChain(): Boolean = this == CHAIN
    fun isIndividual(): Boolean = this == INDIVIDUAL
}