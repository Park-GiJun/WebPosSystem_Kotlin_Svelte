package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.application.port.out.StoreRepository
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.StoreId
import kotlinx.coroutines.flow.Flow
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
        val savedEntity = r2dbcRepository.save(entity)
        return storeMapper.toDomain(savedEntity)
    }

    override suspend fun findByStoreId(storeId: StoreId): Store? {
        return r2dbcRepository.findByStoreId(storeId)?.let { 
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
        return r2dbcRepository.findByStoreType(storeType).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByOperationType(operationType: OperationType): Flow<Store> {
        return r2dbcRepository.findByOperationType(operationType).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByStoreStatus(storeStatus: StoreStatus): Flow<Store> {
        return r2dbcRepository.findByStoreStatus(storeStatus).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByRegionCode(regionCode: String): Flow<Store> {
        return r2dbcRepository.findByRegionCode(regionCode).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByOwnerName(ownerName: String): Flow<Store> {
        return r2dbcRepository.findByOwnerName(ownerName).map { storeMapper.toDomain(it) }
    }

    // ===== 본사 관련 조회 =====
    
    override suspend fun findByHqId(hqId: HeadquartersId): Flow<Store> {
        return r2dbcRepository.findByHqId(hqId).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByHqIdAndIsActive(hqId: HeadquartersId, isActive: Boolean): Flow<Store> {
        return r2dbcRepository.findByHqIdAndIsActive(hqId, isActive).map { storeMapper.toDomain(it) }
    }

    override suspend fun countByHqIdAndIsActive(hqId: HeadquartersId): Long {
        return r2dbcRepository.countByHqIdAndIsActive(hqId)
    }

    // ===== 지역별 조회 =====
    
    override suspend fun findByRegionCodeAndIsActive(regionCode: String, isActive: Boolean): Flow<Store> {
        return r2dbcRepository.findByRegionCodeAndIsActive(regionCode, isActive).map { storeMapper.toDomain(it) }
    }

    override suspend fun countByRegionCodeAndIsActive(regionCode: String): Long {
        return r2dbcRepository.countByRegionCodeAndIsActive(regionCode)
    }

    // ===== 복합 조건 조회 =====
    
    override suspend fun findByStoreTypeAndStoreStatus(storeType: StoreType, storeStatus: StoreStatus): Flow<Store> {
        return r2dbcRepository.findByStoreTypeAndStoreStatus(storeType, storeStatus).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByOperationTypeAndIsActive(operationType: OperationType): Flow<Store> {
        return r2dbcRepository.findByOperationTypeAndIsActive(operationType).map { storeMapper.toDomain(it) }
    }

    override suspend fun findByOwnerNameAndIsActive(ownerName: String, isActive: Boolean): Flow<Store> {
        return r2dbcRepository.findByOwnerNameAndIsActive(ownerName, isActive).map { storeMapper.toDomain(it) }
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
        return r2dbcRepository.findByStoreType(storeType)
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
        return r2dbcRepository.existsByStoreNumber(storeNumber)
    }

    override suspend fun existsByStoreNumberAndIsActive(storeNumber: String, isActive: Boolean): Boolean {
        return r2dbcRepository.existsByStoreNumberAndIsActive(storeNumber, isActive)
    }

    override suspend fun existsByStoreName(storeName: String): Boolean {
        return r2dbcRepository.findByStoreName(storeName) != null
    }
}