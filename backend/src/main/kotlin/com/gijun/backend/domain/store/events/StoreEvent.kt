package com.gijun.backend.domain.store.events

import com.gijun.backend.domain.store.vo.*
import com.gijun.backend.domain.store.enums.*
import java.time.LocalDateTime

sealed class StoreEvent(
    open val storeId: StoreId,
    open val occurredAt: LocalDateTime = LocalDateTime.now()
)

data class StoreCreatedEvent(
    override val storeId: StoreId,
    val storeName: String,
    val storeType: StoreType,
    val operationType: OperationType?,
    val regionCode: String,
    val storeNumber: String,
    val ownerName: String?,
    val headquartersId: HeadquartersId?,
    val createdBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)

data class StoreDeletedEvent(
    override val storeId: StoreId,
    val reason: String?,
    val deletedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)

data class StoreStatusChangedEvent(
    override val storeId: StoreId,
    val previousStatus: StoreStatus,
    val newStatus: StoreStatus,
    val reason: String?,
    val changedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)

data class StoreInfoUpdatedEvent(
    override val storeId: StoreId,
    val updatedFields: Map<String, Any?>,
    val updatedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)

data class StoreOperationTypeChangedEvent(
    override val storeId: StoreId,
    val previousOperationType: OperationType?,
    val newOperationType: OperationType?,
    val reason: String?,
    val changedBy: String,
    override val occurredAt: LocalDateTime = LocalDateTime.now()
) : StoreEvent(storeId, occurredAt)
