package com.gijun.backend.domain.organization.entities

import com.gijun.backend.domain.organization.vo.*
import com.gijun.backend.domain.organization.enums.*
import com.gijun.backend.domain.organization.events.*
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
    val managerUserId: String? = null,
    override val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val createdBy: String? = null,
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val updatedBy: String? = null,
    override val deletedAt: LocalDateTime? = null,
    override val deletedBy: String? = null
) : AuditableEntity {

    private val _events = mutableListOf<OrganizationEvent>()
    val events: List<OrganizationEvent> get() = _events.toList()

    init {
        validateStore()
        _events.add(
            StoreCreatedEvent(
                storeId = storeId,
                storeName = storeName,
                storeType = storeType,
                createdBy = createdBy ?: "system"
            )
        )
    }

    private fun validateStore() {
        if (storeType == StoreType.CHAIN) {
            require(hqId != null) { "체인 매장은 본사 ID가 필수입니다." }
            require(operationType != null) { "체인 매장은 운영 형태가 필수입니다." }
        }
        require(storeName.isNotBlank()) { "매장명은 필수입니다." }
        require(ownerName.isNotBlank()) { "점주명은 필수입니다." }
        require(regionCode.isNotBlank()) { "지역 코드는 필수입니다." }
        require(storeNumber.isNotBlank()) { "매장 번호는 필수입니다." }
    }

    fun changeStatus(newStatus: StoreStatus, changedBy: String): Store {
        val updated = this.copy(
            storeStatus = newStatus,
            updatedAt = LocalDateTime.now(),
            updatedBy = changedBy
        )
        
        updated._events.add(
            StoreStatusChangedEvent(
                storeId = storeId,
                previousStatus = this.storeStatus,
                newStatus = newStatus,
                changedBy = changedBy
            )
        )
        
        return updated
    }

    fun assignManager(managerUserId: String, updatedBy: String): Store {
        val updated = this.copy(
            managerUserId = managerUserId,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
        
        updated._events.add(
            StoreManagerAssignedEvent(
                storeId = storeId,
                previousManagerId = this.managerUserId,
                newManagerId = managerUserId,
                assignedBy = updatedBy
            )
        )
        
        return updated
    }

    fun updateInfo(
        storeName: String? = null,
        ownerName: String? = null,
        phoneNumber: PhoneNumber? = null,
        address: String? = null,
        postalCode: String? = null,
        updatedBy: String
    ): Store {
        val updated = this.copy(
            storeName = storeName ?: this.storeName,
            ownerName = ownerName ?: this.ownerName,
            phoneNumber = phoneNumber ?: this.phoneNumber,
            address = address ?: this.address,
            postalCode = postalCode ?: this.postalCode,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
        
        updated._events.add(
            StoreUpdatedEvent(
                storeId = storeId,
                changes = mapOf(
                    "storeName" to (storeName ?: this.storeName),
                    "ownerName" to (ownerName ?: this.ownerName)
                ),
                updatedBy = updatedBy
            )
        )
        
        return updated
    }

    fun delete(deletedBy: String): Store {
        val deleted = this.copy(
            isActive = false,
            storeStatus = StoreStatus.CLOSED,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy
        )
        
        deleted._events.add(
            StoreDeletedEvent(
                storeId = storeId,
                deletedBy = deletedBy
            )
        )
        
        return deleted
    }

    companion object {
        fun createChainStore(
            storeName: String,
            hqId: HeadquartersId,
            regionCode: String,
            storeNumber: String,
            ownerName: String,
            operationType: OperationType,
            createdBy: String,
            businessLicense: BusinessLicense? = null,
            phoneNumber: PhoneNumber? = null,
            address: String? = null,
            postalCode: String? = null,
            openingDate: LocalDate? = null
        ): Store {
            return Store(
                storeId = StoreId.generateChainStore(hqId.value, regionCode, storeNumber),
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
                openingDate = openingDate ?: LocalDate.now(),
                createdBy = createdBy
            )
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
            openingDate: LocalDate? = null
        ): Store {
            return Store(
                storeId = StoreId.generateIndividualStore(regionCode, storeNumber),
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
                openingDate = openingDate ?: LocalDate.now(),
                createdBy = createdBy
            )
        }
    }
}
