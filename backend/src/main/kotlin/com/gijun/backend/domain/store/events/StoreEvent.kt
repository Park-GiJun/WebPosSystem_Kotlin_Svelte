package com.gijun.backend.domain.store.events

import com.gijun.backend.domain.store.vo.StoreId
import java.time.LocalDateTime

sealed class StoreEvent(
    open val storeId: StoreId,
    open val occurredAt: LocalDateTime = LocalDateTime.now()
)

data class StoreCreatedEvent(
    override val storeId: StoreId,
    val storeName: String,
    val createdBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)

data class StoreStatusChangedEvent(
    override val storeId: StoreId,
    val previousStatus: String,
    val newStatus: String,
    val changedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)

data class StoreDeletedEvent(
    override val storeId: StoreId,
    val deletedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)