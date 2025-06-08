package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StoreR2dbcRepository : CoroutineCrudRepository<StoreEntity, String> {
    
    // 기본 조회 메서드
    suspend fun findByStoreId(storeId: StoreId): StoreEntity?
    suspend fun findByStoreName(storeName: String): StoreEntity?
    suspend fun findByStoreNameAndIsActive(storeName: String, isActive: Boolean): StoreEntity?
    suspend fun findByStoreType(storeType: StoreType): Flow<StoreEntity>
    suspend fun findByOperationType(operationType: OperationType): Flow<StoreEntity>
    suspend fun findByHqId(hqId: HeadquartersId): Flow<StoreEntity>
    suspend fun findByRegionCode(regionCode: String): Flow<StoreEntity>
    suspend fun findByStoreStatus(storeStatus: StoreStatus): Flow<StoreEntity>
    suspend fun findByOwnerName(ownerName: String): Flow<StoreEntity>
    
    // 활성 매장 조회
    @Query("SELECT * FROM stores WHERE is_active = true ORDER BY created_at DESC")
    fun findActiveStores(): Flow<StoreEntity>
    
    // 본사별 매장 조회
    @Query("SELECT * FROM stores WHERE hq_id = :hqId AND is_active = :isActive ORDER BY store_name")
    fun findByHqIdAndIsActive(hqId: HeadquartersId, isActive: Boolean): Flow<StoreEntity>
    
    // 지역별 매장 조회
    @Query("SELECT * FROM stores WHERE region_code = :regionCode AND is_active = :isActive ORDER BY store_name")
    fun findByRegionCodeAndIsActive(regionCode: String, isActive: Boolean): Flow<StoreEntity>
    
    // 매장 타입 및 상태별 조회
    @Query("SELECT * FROM stores WHERE store_type = :storeType AND store_status = :storeStatus AND is_active = true")
    fun findByStoreTypeAndStoreStatus(storeType: StoreType, storeStatus: StoreStatus): Flow<StoreEntity>
    
    // 운영 형태별 조회
    @Query("SELECT * FROM stores WHERE operation_type = :operationType AND is_active = true ORDER BY store_name")
    fun findByOperationTypeAndIsActive(operationType: OperationType): Flow<StoreEntity>
    
    // 매장번호 중복 체크
    suspend fun existsByStoreNumber(storeNumber: String): Boolean
    suspend fun existsByStoreNumberAndIsActive(storeNumber: String, isActive: Boolean): Boolean
    
    // 본사별 매장 수 계산
    @Query("SELECT COUNT(*) FROM stores WHERE hq_id = :hqId AND is_active = true")
    suspend fun countByHqIdAndIsActive(hqId: HeadquartersId): Long
    
    // 지역별 매장 수 계산
    @Query("SELECT COUNT(*) FROM stores WHERE region_code = :regionCode AND is_active = true")
    suspend fun countByRegionCodeAndIsActive(regionCode: String): Long
    
    // 매장 상태별 수 계산
    @Query("SELECT COUNT(*) FROM stores WHERE store_status = :storeStatus AND is_active = true")
    suspend fun countByStoreStatusAndIsActive(storeStatus: StoreStatus): Long
    
    // 검색 기능
    @Query("""
        SELECT * FROM stores 
        WHERE (store_name LIKE CONCAT('%', :searchTerm, '%') 
               OR owner_name LIKE CONCAT('%', :searchTerm, '%')
               OR address LIKE CONCAT('%', :searchTerm, '%'))
        AND is_active = :isActive 
        ORDER BY store_name
    """)
    fun searchStores(searchTerm: String, isActive: Boolean): Flow<StoreEntity>
    
    // 최근 생성된 매장 조회
    @Query("SELECT * FROM stores WHERE is_active = true ORDER BY created_at DESC LIMIT :limit")
    fun findRecentStores(limit: Int): Flow<StoreEntity>
    
    // 특정 기간 생성된 매장 조회
    @Query("""
        SELECT * FROM stores 
        WHERE created_at BETWEEN :startDate AND :endDate 
        AND is_active = :isActive 
        ORDER BY created_at DESC
    """)
    fun findStoresByDateRange(startDate: String, endDate: String, isActive: Boolean): Flow<StoreEntity>
    
    // 점주별 매장 조회 (개인매장 점주가 여러 매장을 운영하는 경우)
    @Query("SELECT * FROM stores WHERE owner_name = :ownerName AND is_active = :isActive ORDER BY store_name")
    fun findByOwnerNameAndIsActive(ownerName: String, isActive: Boolean): Flow<StoreEntity>
}