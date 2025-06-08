package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreR2dbcRepository : CoroutineCrudRepository<StoreEntity, String> {
    
    // 기본 조회 메서드
    suspend fun findByStoreId(storeId: String): StoreEntity?
    suspend fun findByStoreName(storeName: String): StoreEntity?
    suspend fun findByStoreNameAndIsActive(storeName: String, isActive: Boolean): StoreEntity?
    suspend fun findByStoreType(storeType: StoreType): Flow<StoreEntity>
    suspend fun findByOperationType(operationType: OperationType): Flow<StoreEntity>
    suspend fun findByHqId(hqId: String): Flow<StoreEntity>
    suspend fun findByRegionCode(regionCode: String): Flow<StoreEntity>
    suspend fun findByStoreStatus(store