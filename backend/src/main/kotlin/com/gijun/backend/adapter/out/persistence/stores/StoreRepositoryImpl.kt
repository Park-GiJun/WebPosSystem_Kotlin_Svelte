package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.application.port.out.StoreRepository
import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.vo.StoreId
import org.springframework.stereotype.Component

@Component
class StoreRepositoryImpl (
    private val r2dbcRepository: StoreR2dbcRepository,
    private val storeMapper: StoreMapper
) : StoreRepository {
    override suspend fun save(store: Store): Store {
        TODO("Not yet implemented")
    }

    override suspend fun findByStoreId(storeId: StoreId): Store? {
        TODO("Not yet implemented")
    }
}