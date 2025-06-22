package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.application.port.out.StoreRepository
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class StoreRepositoryImpl(
    private val r2dbcRepository: StoreR2dbcRepository,
    private val storeMapper: StoreMapper
) : StoreRepository {

    // ===== 기본 CRUD 작업 =====
    
    override suspend fun save(store: Store): Store {
        val entity = storeMapper.toEntity(store)
        
        // 새로운 매장 생성 시에는 INSERT 쿼리 사용
        val existingEntity = r2dbcRepository.findByStoreId(entity.storeId)
        if (existingEntity == null) {
            // 새로운 엔티티면 INSERT
            r2dbcRepository.insertStore(entity)
            return store
        } else {
            // 기존 엔티티면 UPDATE
            val savedEntity = r2dbcRepository.save(entity)
            return storeMapper.toDomain(savedEntity)
        }
    }

    override suspend fun findByStoreId(storeId: StoreId): Store? {
        return r2dbcRepository.findByStoreId(storeId.value)?.let { 
            storeMapper.toDomain(it) 
        }
    }

    override suspend fun findByStoreName(storeName: String): Store? {
        return r2dbcRepository.findByStoreName(storeName)?.let { 
            storeMapper.toDomain(it) 
        }
    }

    override suspend fun findByStoreNameAndIsActive(storeName: String, isActive: Boolean): Store? {
        return r2dbcRepository.findByStoreNameAndIsActive(storeName, isActive)?.let { 
            storeMapper.toDomain(it) 
        }
    }

    override suspend fun deleteByStoreId(storeId: StoreId) {
        r2dbcRepository.deleteById(storeId.value)
    }

    override suspend fun existsByStoreId(storeId: StoreId): Boolean {
        return r2dbcRepository.existsById(storeId.value)
    }

    // ===== 조회 메서드 =====
    
    override suspend fun findAll(): Flow<Store> {
        return r2dbcRepository.findAll().map { storeMapper.toDomain(it) }
    }

    override suspend fun findActiveStores(): Flow<Store> {
        return r2dbcRepository.findActiveStores().map { storeMapper.toDomain(it) }
    }

    override suspend fun findByStoreType(storeType: StoreType): Flow<Store> {
        // 실제 구현 필요 시 store_type 문자열로 검색
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun findByOperationType(operationType: OperationType): Flow<Store> {
        // 현재 stores 테이블에 operation_type 컬럼이 없으므로 빈 Flow 반환
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun findByStoreStatus(storeStatus: StoreStatus): Flow<Store> {
        return r2dbcRepository.findByStoreStatus(storeStatus).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByRegionCode(regionCode: String): Flow<Store> {
        return r2dbcRepository.findByStoreCode(regionCode).let { entity ->
            if (entity != null) {
                kotlinx.coroutines.flow.flowOf(storeMapper.toDomain(entity))
            } else {
                kotlinx.coroutines.flow.emptyFlow()
            }
        }
    }

    override suspend fun findByOwnerName(ownerName: String): Flow<Store> {
        // 현재 테이블에 owner_name 컬럼이 없으므로 빈 Flow 반환
        return kotlinx.coroutines.flow.emptyFlow()
    }

    // ===== 본사 관련 조회 =====
    
    override suspend fun findByHqId(hqId: HeadquartersId): Flow<Store> {
        return r2dbcRepository.findByHqIdAndIsActive(hqId.value, true).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByHqIdAndIsActive(hqId: HeadquartersId, isActive: Boolean): Flow<Store> {
        return r2dbcRepository.findByHqIdAndIsActive(hqId.value, isActive).map { storeMapper.toDomain(it) }
    }

    override suspend fun countByHqIdAndIsActive(hqId: HeadquartersId): Long {
        return r2dbcRepository.countByHqIdAndIsActive(hqId.value)
    }

    // ===== 지역별 조회 =====
    
    override suspend fun findByRegionCodeAndIsActive(regionCode: String, isActive: Boolean): Flow<Store> {
        return r2dbcRepository.findByStoreCodeAndIsActive(regionCode, isActive).map { storeMapper.toDomain(it) }
    }

    override suspend fun countByRegionCodeAndIsActive(regionCode: String): Long {
        return r2dbcRepository.findByStoreCodeAndIsActive(regionCode, true)
            .map { 1L }
            .fold(0L) { acc, _ -> acc + 1 }
    }

    // ===== 복합 조건 조회 =====
    
    override suspend fun findByStoreTypeAndStoreStatus(storeType: StoreType, storeStatus: StoreStatus): Flow<Store> {
        return r2dbcRepository.findByStoreStatus(storeStatus)
            .map { storeMapper.toDomain(it) }
            .filter { it.storeType == storeType }
    }

    override suspend fun findByOperationTypeAndIsActive(operationType: OperationType): Flow<Store> {
        // 현재 stores 테이블에 operation_type 컬럼이 없으므로 빈 Flow 반환
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun findByOwnerNameAndIsActive(ownerName: String, isActive: Boolean): Flow<Store> {
        // 현재 테이블에 owner_name 컬럼이 없으므로 빈 Flow 반환
        return kotlinx.coroutines.flow.emptyFlow()
    }

    // ===== 검색 기능 =====
    
    override suspend fun searchStores(searchTerm: String, isActive: Boolean): Flow<Store> {
        return r2dbcRepository.searchStores(searchTerm, isActive).map { storeMapper.toDomain(it) }
    }

    override suspend fun findRecentStores(limit: Int): Flow<Store> {
        return r2dbcRepository.findRecentStores(limit).map { storeMapper.toDomain(it) }
    }

    override suspend fun findStoresByDateRange(startDate: String, endDate: String, isActive: Boolean): Flow<Store> {
        return r2dbcRepository.findStoresByDateRange(startDate, endDate, isActive).map { storeMapper.toDomain(it) }
    }

    // ===== 통계 및 카운트 =====
    
    override suspend fun countByStoreStatusAndIsActive(storeStatus: StoreStatus): Long {
        return r2dbcRepository.countByStoreStatusAndIsActive(storeStatus)
    }

    override suspend fun countByStoreType(storeType: StoreType): Long {
        return r2dbcRepository.findActiveStores()
            .map { storeMapper.toDomain(it) }
            .filter { it.storeType == storeType }
            .map { 1L }
            .fold(0L) { acc, _ -> acc + 1 }
    }

    override suspend fun countActiveStores(): Long {
        return r2dbcRepository.findActiveStores()
            .map { 1L }
            .fold(0L) { acc, _ -> acc + 1 }
    }

    // ===== 중복 체크 =====
    
    override suspend fun existsByStoreNumber(storeNumber: String): Boolean {
        return r2dbcRepository.existsByStoreCode(storeNumber)
    }

    override suspend fun existsByStoreNumberAndIsActive(storeNumber: String, isActive: Boolean): Boolean {
        return r2dbcRepository.existsByStoreCodeAndIsActive(storeNumber, isActive)
    }

    override suspend fun existsByStoreName(storeName: String): Boolean {
        return r2dbcRepository.findByStoreName(storeName) != null
    }
}