package com.gijun.backend.adapter.out.persistence.headquarters

import com.gijun.backend.application.port.out.HeadquartersRepository
import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId
import org.springframework.stereotype.Component

@Component
class HeadquartersRepositoryImpl(
    private val r2dbcRepository: HeadquartersR2dbcRepository,
    private val headquartersMapper: HeadquartersMapper
) : HeadquartersRepository {
    override suspend fun save(headquarters: Headquarters) {
        TODO("Not yet implemented")
    }

    override suspend fun findByHqId(hqId: HeadquartersId): Headquarters? {
        TODO("Not yet implemented")
    }

    override suspend fun findByStoreType(storeType: StoreType): List<Headquarters> {
        TODO("Not yet implemented")
    }

    override suspend fun findByHqStatus(hqStatus: StoreStatus): List<Headquarters> {
        TODO("Not yet implemented")
    }

    override suspend fun findActiveHeadquarters(): List<Headquarters> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByHqId(hqId: HeadquartersId) {
        TODO("Not yet implemented")
    }

}