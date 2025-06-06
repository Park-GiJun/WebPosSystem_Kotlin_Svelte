package com.gijun.backend.domain.store.entities

import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.PhoneNumber
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

    fun delete(deletedBy: String): Headquarters =
        this.copy(
            isActive = false,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy
        )

    fun activate(updatedBy: String): Headquarters =
        this.copy(
            isActive = true,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    fun deactivate(updatedBy: String): Headquarters =
        this.copy(
            isActive = false,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

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
            require(hqCode.isNotBlank()) { "본사 코드는 필수입니다." }
            require(hqName.isNotBlank()) { "본사명은 필수입니다." }
            require(createdBy.isNotBlank()) { "생성자는 필수입니다." }

            return Headquarters(
                hqId = HeadquartersId.generate(hqCode),
                hqCode = hqCode.uppercase(),
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