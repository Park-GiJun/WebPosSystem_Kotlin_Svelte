package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

/**
 * Store 조회 전용 레포지토리
 * 복잡한 쿼리나 집계 작업을 처리
 */
@Component
class StoreQueryRepository(
    private val r2dbcRepository: StoreR2dbcRepository,
    private val storeMapper: StoreMapper
) {

    /**
     * 대시보드용 매장 통계 조회
     */
    suspend fun getStoreStatistics(): StoreStatistics {
        val activeStores = r2dbcRepository.findActiveStores()
        val totalCount = activeStores.map { 1L }.fold(0L) { acc, _ -> acc + 1 }
        
        val chainStoreCount = r2dbcRepository.findByStoreTypeAndStoreStatus(StoreType.CHAIN, StoreStatus.ACTIVE)
            .map { 1L }.fold(0L) { acc, _ -> acc + 1 }
            
        val individualStoreCount = r2dbcRepository.findByStoreTypeAndStoreStatus(StoreType.INDIVIDUAL, StoreStatus.ACTIVE)
            .map { 1L }.fold(0L) { acc, _ -> acc + 1 }

        return StoreStatistics(
            totalActiveStores = totalCount,
            chainStores = chainStoreCount,
            individualStores = individualStoreCount,
            inactiveStores = 0L // TODO: 비활성 매장 카운트 추가
        )
    }

    /**
     * 본사별 매장 통계 조회
     */
    suspend fun getStoreStatisticsByHq(hqId: HeadquartersId): HqStoreStatistics {
        val totalStores = r2dbcRepository.countByHqIdAndIsActive(hqId)
        
        val directStores = r2dbcRepository.findByOperationTypeAndIsActive(OperationType.DIRECT)
            .map { storeMapper.toDomain(it) }
            .filter { it.hqId == hqId }
            .map { 1L }
            .fold(0L) { acc, _ -> acc + 1 }
            
        val franchiseStores = r2dbcRepository.findByOperationTypeAndIsActive(OperationType.FRANCHISE)
            .map { storeMapper.toDomain(it) }
            .filter { it.hqId == hqId }
            .map { 1L }
            .fold(0L) { acc, _ -> acc + 1 }

        return HqStoreStatistics(
            headquartersId = hqId,
            totalStores = totalStores,
            directStores = directStores,
            franchiseStores = franchiseStores
        )
    }

    /**
     * 지역별 매장 분포 조회
     */
    suspend fun getStoreDistributionByRegion(): Flow<RegionStoreDistribution> {
        return r2dbcRepository.findActiveStores()
            .map { entity ->
                RegionStoreDistribution(
                    regionCode = entity.regionCode,
                    storeName = entity.storeName,
                    storeType = entity.storeType,
                    operationType = entity.operationType,
                    storeStatus = entity.storeStatus
                )
            }
    }

    /**
     * 최근 활동 매장 조회 (최근 업데이트된 매장)
     */
    suspend fun findRecentlyUpdatedStores(limit: Int): Flow<Store> {
        return r2dbcRepository.findRecentStores(limit)
            .map { storeMapper.toDomain(it) }
    }

    /**
     * 운영 상태별 매장 요약
     */
    suspend fun getOperationStatusSummary(): Flow<OperationStatusSummary> {
        return r2dbcRepository.findActiveStores()
            .map { entity ->
                OperationStatusSummary(
                    storeType = entity.storeType,
                    operationType = entity.operationType,
                    storeStatus = entity.storeStatus,
                    count = 1L
                )
            }
    }
}

/**
 * 매장 통계 데이터 클래스
 */
data class StoreStatistics(
    val totalActiveStores: Long,
    val chainStores: Long,
    val individualStores: Long,
    val inactiveStores: Long
)

/**
 * 본사별 매장 통계 데이터 클래스
 */
data class HqStoreStatistics(
    val headquartersId: HeadquartersId,
    val totalStores: Long,
    val directStores: Long,
    val franchiseStores: Long
)

/**
 * 지역별 매장 분포 데이터 클래스
 */
data class RegionStoreDistribution(
    val regionCode: String,
    val storeName: String,
    val storeType: StoreType,
    val operationType: OperationType?,
    val storeStatus: StoreStatus
)

/**
 * 운영 상태별 요약 데이터 클래스
 */
data class OperationStatusSummary(
    val storeType: StoreType,
    val operationType: OperationType?,
    val storeStatus: StoreStatus,
    val count: Long
)