package com.gijun.backend.domain.store.entities

import com.gijun.backend.domain.store.vo.*
import com.gijun.backend.domain.store.enums.*
import com.gijun.backend.domain.store.events.*
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
    val storeType: StoreType = StoreType.FRANCHISE,
    val hqStatus: StoreStatus = StoreStatus.ACTIVE,
    override val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val createdBy: String? = null,
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val updatedBy: String? = null,
    override val deletedAt: LocalDateTime? = null,
    override val deletedBy: String? = null
) : AuditableEntity {

    init {
        require(hqCode.isNotBlank()) { "본사 코드는 필수입니다." }
        require(hqName.isNotBlank()) { "본사명은 필수입니다." }
        require(hqCode.length >= 2) { "본사 코드는 최소 2자 이상이어야 합니다." }
        require(hqName.length >= 2) { "본사명은 최소 2자 이상이어야 합니다." }
    }

    fun isOperational(): Boolean = hqStatus == StoreStatus.ACTIVE && isActive

    fun updateInfo(
        hqName: String? = null,
        businessLicense: BusinessLicense? = null,
        ceoName: String? = null,
        headquartersAddress: String? = null,
        contactPhone: PhoneNumber? = null,
        website: String? = null,
        updatedBy: String
    ): Headquarters = this.copy(
        hqName = hqName ?: this.hqName,
        businessLicense = businessLicense ?: this.businessLicense,
        ceoName = ceoName ?: this.ceoName,
        headquartersAddress = headquartersAddress ?: this.headquartersAddress,
        contactPhone = contactPhone ?: this.contactPhone,
        website = website ?: this.website,
        updatedAt = LocalDateTime.now(),
        updatedBy = updatedBy
    )

    fun activate(updatedBy: String): Headquarters =
        this.copy(
            hqStatus = StoreStatus.ACTIVE,
            isActive = true,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    fun deactivate(updatedBy: String): Headquarters =
        this.copy(
            hqStatus = StoreStatus.INACTIVE,
            isActive = false,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    fun suspend(updatedBy: String): Headquarters =
        this.copy(
            hqStatus = StoreStatus.SUSPENDED,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

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
