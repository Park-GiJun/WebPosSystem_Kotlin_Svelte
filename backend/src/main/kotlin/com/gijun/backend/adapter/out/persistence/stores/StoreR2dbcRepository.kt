package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.StoreId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreR2dbcRepository : CoroutineCrudRepository<StoreEntity, String> {
    suspend fun findByStoreId(storeId : StoreId): StoreEntity?
    suspend fun findByStoreName(storeName : String) : StoreEntity?
    suspend fun findByStoreType(storeType : StoreType) : StoreEntity?
    suspend fun findByOperationType(operationType : OperationType) : StoreEntity?
    suspend fun findByHqId(hqId : HeadquartersId) : StoreEntity?
    suspend fun findByRegionCode(regionCode : String) : StoreEntity?
}