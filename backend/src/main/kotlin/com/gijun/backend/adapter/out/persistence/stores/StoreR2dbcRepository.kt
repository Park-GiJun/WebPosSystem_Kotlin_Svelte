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

    // 명시적 INSERT 쿼리 - 실제 테이블 스키마에 맞춤
    @Query("""
        INSERT INTO stores (store_id, store_code, store_name, hq_id, store_address, 
                           contact_phone, store_manager_id, registration_date, business_hours, 
                           store_type, store_status, is_active, created_at, created_by, 
                           updated_at, updated_by, version)
        VALUES (:#{#entity.storeId}, :#{#entity.storeCode}, :#{#entity.storeName}, 
                :#{#entity.hqId}, :#{#entity.storeAddress}, :#{#entity.contactPhone}, 
                :#{#entity.storeManagerId}, :#{#entity.registrationDate}, :#{#entity.businessHours}, 
                :#{#entity.storeType}, :#{#entity.storeStatus}, :#{#entity.isActive}, 
                :#{#entity.createdAt}, :#{#entity.createdBy}, :#{#entity.updatedAt}, 
                :#{#entity.updatedBy}, :#{#entity.version})
    """)
    suspend fun insertStore(entity: StoreEntity): Int

    // 기본 조회 메서드
    suspend fun findByStoreId(storeId: String): StoreEntity?
    suspend fun findByStoreName(storeName: String): StoreEntity?
    suspend fun findByStoreNameAndIsActive(storeName: String, isActive: Boolean): StoreEntity?
    suspend fun findByStoreCode(storeCode: String): StoreEntity?

    // 활성 매장 조회
    @Query("SELECT * FROM stores WHERE is_active = true ORDER BY created_at DESC")
    fun findActiveStores(): Flow<StoreEntity>

    // 본사별 매장 조회
    @Query("SELECT * FROM stores WHERE hq_id = :hqId AND is_active = :isActive ORDER BY store_name")
    fun findByHqIdAndIsActive(hqId: String, isActive: Boolean): Flow<StoreEntity>

    // 매장 코드별 조회
    @Query("SELECT * FROM stores WHERE store_code = :storeCode AND is_active = :isActive ORDER BY store_name")
    fun findByStoreCodeAndIsActive(storeCode: String, isActive: Boolean): Flow<StoreEntity>

    // 매장 상태별 조회
    @Query("SELECT * FROM stores WHERE store_status = :storeStatus AND is_active = true")
    fun findByStoreStatus(storeStatus: StoreStatus): Flow<StoreEntity>

    // 매장 코드 중복 체크
    suspend fun existsByStoreCode(storeCode: String): Boolean
    suspend fun existsByStoreCodeAndIsActive(storeCode: String, isActive: Boolean): Boolean

    // 본사별 매장 수 계산
    @Query("SELECT COUNT(*) FROM stores WHERE hq_id = :hqId AND is_active = true")
    suspend fun countByHqIdAndIsActive(hqId: String): Long

    // 매장 상태별 수 계산
    @Query("SELECT COUNT(*) FROM stores WHERE store_status = :storeStatus AND is_active = true")
    suspend fun countByStoreStatusAndIsActive(storeStatus: StoreStatus): Long

    // 검색 기능
    @Query("""
        SELECT * FROM stores 
        WHERE (store_name LIKE CONCAT('%', :searchTerm, '%') 
               OR store_code LIKE CONCAT('%', :searchTerm, '%')
               OR store_address LIKE CONCAT('%', :searchTerm, '%'))
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
}