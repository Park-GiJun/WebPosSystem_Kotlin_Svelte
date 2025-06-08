package com.gijun.backend.application.port.out

import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.Flow

interface StoreRepository {

    // 기본 CRUD 작업
    suspend fun save(store: Store): Store
    suspend fun findByStoreId(storeId: StoreId): Store?
    suspend fun findByStoreName(storeName: String): Store?
    suspend fun findByStoreNameAndIsActive(storeName: String, isActive: Boolean): Store?
    suspend fun deleteByStoreId(storeId: StoreId)
    suspend fun existsByStoreId(storeId: StoreId): Boolean
    
    // 조회 메서드
    suspend fun findAll(): Flow<Store>
    suspend fun findActiveStores(): Flow<Store>
    suspend fun findByStoreType(storeType: StoreType): Flow<Store>
    suspend fun findByOperationType(operationType: OperationType): Flow<Store>
    suspend fun findByStoreStatus(storeStatus: StoreStatus): Flow<Store>
    suspend fun findByRegionCode(regionCode: String): Flow<Store>
    suspend fun findByOwnerName(ownerName: String): Flow<Store>
    
    // 본사 관련 조회
    suspend fun findByHqId(hqId: HeadquartersId): Flow<Store>
    suspend fun findByHqIdAndIsActive(hqId: HeadquartersId, isActive: Boolean): Flow<Store>
    suspend fun countByHqIdAndIsActive(hqId: HeadquartersId): Long
    
    // 지역별 조회
    suspend fun findByRegionCodeAndIsActive(regionCode: String, isActive: Boolean): Flow<Store>
    suspend fun countByRegionCodeAndIsActive(regionCode: String): Long
    
    // 복합 조건 조회
    suspend fun findByStoreTypeAndStoreStatus(storeType: StoreType, storeStatus: StoreStatus): Flow<Store>
    suspend fun findByOperationTypeAndIsActive(operationType: OperationType): Flow<Store>
    suspend fun findByOwnerNameAndIsActive(ownerName: String, isActive: Boolean): Flow<Store>
    
    // 검색 기능
    suspend fun searchStores(searchTerm: String, isActive: Boolean): Flow<Store>
    suspend fun findRecentStores(limit: Int): Flow<Store>
    suspend fun findStoresByDateRange(startDate: String, endDate: String, isActive: Boolean): Flow<Store>
    
    // 통계 및 카운트
    suspend fun countByStoreStatusAndIsActive(storeStatus: StoreStatus): Long
    suspend fun countByStoreType(storeType: StoreType): Long
    suspend fun countActiveStores(): Long
    
    // 중복 체크
    suspend fun existsByStoreNumber(storeNumber: String): Boolean
    suspend fun existsByStoreNumberAndIsActive(storeNumber: String, isActive: Boolean): Boolean
    suspend fun existsByStoreName(storeName: String): Boolean
}