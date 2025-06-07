package com.gijun.backend.domain.organization.entities

import com.gijun.backend.domain.organization.vo.*
import com.gijun.backend.domain.organization.enums.*
import com.gijun.backend.domain.organization.events.*
import com.gijun.backend.domain.common.AuditableEntity
import java.time.LocalDateTime

data class Headquarters(
    val hqId: HeadquartersId,
    val hqCode: String,
    val hqName: String,
    val businessLicense: BusinessLicense? = null,
    val ceoName: String? = null,
    val headquartersAddress: String? = null,
    val contactPhone: PhoneNumber? = null,
    val website: String? = null,
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
        validateHeadquarters()
        _events.add(
            HeadquartersCreatedEvent(
                hqId = hqId,
                hqCode = hqCode,
                hqName = hqName,
                createdBy = createdBy ?: "system"
            )
        )
    }

    private fun validateHeadquarters() {
        require(hqCode.isNotBlank()) { "본사 코드는 필수입니다." }
        require(hqName.isNotBlank()) { "본사명은 필수입니다." }
        require(hqCode.length <= 10) { "본사 코드는 10자 이하여야 합니다." }
        require(hqName.length <= 100) { "본사명은 100자 이하여야 합니다." }
    }

    fun updateInfo(
        hqName: String? = null,
        businessLicense: BusinessLicense? = null,
        ceoName: String? = null,
        headquartersAddress: String? = null,
        contactPhone: PhoneNumber? = null,
        website: String? = null,
        updatedBy: String
    ): Headquarters {
        val updated = this.copy(
            hqName = hqName ?: this.hqName,
            businessLicense = businessLicense ?: this.businessLicense,
            ceoName = ceoName ?: this.ceoName,
            headquartersAddress = headquartersAddress ?: this.headquartersAddress,
            contactPhone = contactPhone ?: this.contactPhone,
            website = website ?: this.website,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
        
        updated._events.add(
            HeadquartersUpdatedEvent(
                hqId = hqId,
                changes = mapOf(
                    "hqName" to (hqName ?: this.hqName),
                    "ceoName" to (ceoName ?: this.ceoName)
                ),
                updatedBy = updatedBy
            )
        )
        
        return updated
    }

    fun delete(deletedBy: String): Headquarters {
        val deleted = this.copy(
            isActive = false,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy
        )
        
        deleted._events.add(
            HeadquartersDeletedEvent(
                hqId = hqId,
                deletedBy = deletedBy
            )
        )
        
        return deleted
    }

    fun activate(updatedBy: String): Headquarters {
        val activated = this.copy(
            isActive = true,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
        
        activated._events.add(
            HeadquartersStatusChangedEvent(
                hqId = hqId,
                previousStatus = "INACTIVE",
                newStatus = "ACTIVE",
                changedBy = updatedBy
            )
        )
        
        return activated
    }

    fun deactivate(updatedBy: String): Headquarters {
        val deactivated = this.copy(
            isActive = false,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )
        
        deactivated._events.add(
            HeadquartersStatusChangedEvent(
                hqId = hqId,
                previousStatus = "ACTIVE", 
                newStatus = "INACTIVE",
                changedBy = updatedBy
            )
        )
        
        return deactivated
    }

    companion object {
        fun create(
            hqCode: String,
            hqName: String,
            createdBy: String,
            businessLicense: BusinessLicense? = null,
            ceoName: String? = null,
            headquartersAddress: String? = null,
            contactPhone: PhoneNumber? = null,
            website: String? = null
        ): Headquarters {
            return Headquarters(
                hqId = HeadquartersId.generate(hqCode),
                hqCode = hqCode,
                hqName = hqName,
                businessLicense = businessLicense,
                ceoName = ceoName,
                headquartersAddress = headquartersAddress,
                contactPhone = contactPhone,
                website = website,
                createdBy = createdBy
            )
        }
    }
}
