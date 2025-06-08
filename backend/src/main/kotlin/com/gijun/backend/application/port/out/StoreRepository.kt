package com.gijun.backend.application.port.out

import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.vo.StoreId

interface StoreRepository {

    suspend fun save(store: Store) : Store

    suspend fun findByStoreId(storeId: StoreId): Store?

}