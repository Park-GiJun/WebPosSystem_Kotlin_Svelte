package com.gijun.backend.application.port.out

import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.HeadquartersId

interface HeadquartersRepository {
    suspend fun save(headquarters : Headquarters)
    suspend fun findByHqId(hqId : HeadquartersId) : Headquarters?
    suspend fun findAll() : List<Headquarters>
    suspend fun findByStoreType(storeType : StoreType) : List<Headquarters>
    suspend fun findByHqStatus(hqStatus : StoreStatus) : List<Headquarters>
    suspend fun findActiveHeadquarters() : List<Headquarters>
    suspend fun deleteByHqId(hqId : HeadquartersId)
}