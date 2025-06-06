package com.gijun.backend.domain.store.entities

import com.gijun.backend.domain.store.vo.*
import com.gijun.backend.domain.store.enums.*
import com.gijun.backend.domain.store.events.*
import com.gijun.backend.domain.common.AuditableEntity
import java.time.LocalDate
import java.time.LocalDateTime

data class Store(
    val storeId: StoreId,
    val storeName: String,
    val storeType: StoreType,
    val operationType: OperationType? = null,
    val hqId: HeadquartersId? = null,
    val regionCode: String,
    val storeNumber: String,
    val businessLicense: BusinessLicense? = null,
    val ownerName: String,
    val phoneNumber: PhoneNumber? = null,
    val address: String? = null,
    val postalCode: String? = null,
    val openingDate: LocalDate = LocalDate.now(),
    val storeStatus: StoreStatus = StoreStatus.ACTIVE,
    override val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val createdBy: String? = null,
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val updatedBy: String? = null,
    override val deletedAt: LocalDateTime? = null,
    override val deletedBy: String? = null
) : AuditableEntity {

    private val _events = mutableListOf<StoreEvent>()
    val events: List<StoreEvent> get() = _events.toList()

    init {
        validateStore()
    }

    private fun validateStore() {
        if (storeType.isChain()) {
            require(hqId != null) { "체인 매장은 본사 ID가 필수입니다." }
            require(operationType != null) { "체인 매장은 운영 형태가 필수입니다." }
        }
        require(storeName.isNotBlank()) { "매장명은 필수입니다." }
        require(ownerName.isNotBlank()) { "점주명은 필수입니다." }
    }

    fun isChainStore(): Boolean = storeType.isChain()
    fun isIndividualStore(): Boolean = storeType.isIndividual()
    fun isDirect(): Boolean = operationType?.isDirect() == true
    fun isFranchise(): Boolean = operationType?.isFranchise() == true

    fun delete(deletedBy: String): Store {
        val deletedStore = this.copy(
            isActive = false,
            storeStatus = StoreStatus.CLOSED,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy
        )

        deletedStore._events.add(
            StoreDeletedEvent(storeId = storeId, deletedBy = deletedBy)
        )

        return deletedStore
    }

    fun changeStatus(newStatus: StoreStatus, updatedBy: String): Store {
        val updatedStore = this.copy(
            storeStatus = newStatus,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

        updatedStore._events.add(
            StoreStatusChangedEvent(
                storeId = storeId,
                previousStatus = this.storeStatus.name,
                newStatus = newStatus.name,
                changedBy = updatedBy
            )
        )

        return updatedStore
    }

    fun updateInfo(
        storeName: String? = null,
        ownerName: String? = null,
        phoneNumber: PhoneNumber? = null,
        address: String? = null,
        postalCode: String? = null,
        updatedBy: String
    ): Store = this.copy(
        storeName = storeName ?: this.storeName,
        ownerName = ownerName ?: this.ownerName,
        phoneNumber = phoneNumber ?: this.phoneNumber,
        address = address ?: this.address,
        postalCode = postalCode ?: this.postalCode,
        updatedAt = LocalDateTime.now(),
        updatedBy = updatedBy
    )

    fun clearEvents() {
        _events.clear()
    }

    companion object {
        fun createChainStore(
            storeName: String,
            operationType: OperationType,
            hqId: HeadquartersId,
            hqCode: String,
            regionCode: String,
            storeNumber: String,
            ownerName: String,
            createdBy: String,
            businessLicense: BusinessLicense? = null,
            phoneNumber: PhoneNumber? = null,
            address: String? = null,
            postalCode: String? = null,
            openingDate: LocalDate = LocalDate.now()
        ): Store {
            val storeId = StoreId.generateChainStoreId(hqCode, regionCode, storeNumber)

            val store = Store(
                storeId = storeId,
                storeName = storeName,
                storeType = StoreType.CHAIN,
                operationType = operationType,
                hqId = hqId,
                regionCode = regionCode,
                storeNumber = storeNumber,
                businessLicense = businessLicense,
                ownerName = ownerName,
                phoneNumber = phoneNumber,
                address = address,
                postalCode = postalCode,
                openingDate = openingDate,
                createdBy = createdBy
            )

            store._events.add(
                StoreCreatedEvent(
                    storeId = storeId,
                    storeName = storeName,
                    createdBy = createdBy
                )
            )

            return store
        }

        fun createIndividualStore(
            storeName: String,
            regionCode: String,
            storeNumber: String,
            ownerName: String,
            createdBy: String,
            businessLicense: BusinessLicense? = null,
            phoneNumber: PhoneNumber? = null,
            address: String? = null,
            postalCode: String? = null,
            openingDate: LocalDate = LocalDate.now()
        ): Store {
            val storeId = StoreId.generateIndividualStoreId(regionCode, storeNumber)

            val store = Store(
                storeId = storeId,
                storeName = storeName,
                storeType = StoreType.INDIVIDUAL,
                operationType = null,
                hqId = null,
                regionCode = regionCode,
                storeNumber = storeNumber,
                businessLicense = businessLicense,
                ownerName = ownerName,
                phoneNumber = phoneNumber,
                address = address,
                postalCode = postalCode,
                openingDate = openingDate,
                createdBy = createdBy
            )

            store._events.add(
                StoreCreatedEvent(
                    storeId = storeId,
                    storeName = storeName,
                    createdBy = createdBy
                )
            )

            return store
        }
    }
}