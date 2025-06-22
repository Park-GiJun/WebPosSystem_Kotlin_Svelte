package com.gijun.backend.adapter.out.persistence.headquarters

import com.gijun.backend.application.port.out.HeadquartersRepository
import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import org.springframework.stereotype.Component
import kotlinx.coroutines.flow.toList

@Component
class HeadquartersRepositoryImpl(
    private val r2dbcRepository: HeadquartersR2dbcRepository,
    private val headquartersMapper: HeadquartersMapper
) : HeadquartersRepository {
    
    override suspend fun save(headquarters: Headquarters) {
        val entity = headquartersMapper.toEntity(headquarters)
        
        // 새로운 본사 생성 시에는 INSERT 쿼리 사용
        val existingEntity = r2dbcRepository.findById(entity.hqId)
        if (existingEntity == null) {
            // 새로운 엔티티면 INSERT
            r2dbcRepository.insertHeadquarters(entity)
        } else {
            // 기존 엔티티면 UPDATE
            r2dbcRepository.save(entity)
        }
    }

    override suspend fun findByHqId(hqId: HeadquartersId): Headquarters? {
        val entity = r2dbcRepository.findById(hqId.value)
        return entity?.let { headquartersMapper.toDomain(it) }
    }

    override suspend fun findAll(): List<Headquarters> {
        return r2dbcRepository.findAll()
            .toList()
            .map { headquartersMapper.toDomain(it) }
    }

    override suspend fun findByStoreType(storeType: StoreType): List<Headquarters> {
        // 데이터베이스에 store_type 컬럼이 없으므로 모든 본사를 반환
        return r2dbcRepository.findAll()
            .toList()
            .map { headquartersMapper.toDomain(it) }
    }

    override suspend fun findByHqStatus(hqStatus: StoreStatus): List<Headquarters> {
        // 데이터베이스에 hq_status 컬럼이 없으므로 활성 본사를 반환
        return r2dbcRepository.findAll()
            .toList()
            .filter { it.isActive }
            .map { headquartersMapper.toDomain(it) }
    }

    override suspend fun findActiveHeadquarters(): List<Headquarters> {
        return r2dbcRepository.findAll()
            .toList()
            .filter { it.isActive }
            .map { headquartersMapper.toDomain(it) }
    }

    override suspend fun deleteByHqId(hqId: HeadquartersId) {
        r2dbcRepository.deleteById(hqId.value)
    }
}