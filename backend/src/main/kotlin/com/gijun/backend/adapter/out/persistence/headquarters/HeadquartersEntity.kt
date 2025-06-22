package com.gijun.backend.adapter.out.persistence.headquarters

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.LocalDate

@Table("headquarters")
data class HeadquartersEntity(
    @Id
    @Column("hq_id")
    val hqId: String,

    @Column("hq_code")
    val hqCode: String,

    @Column("hq_name")
    val hqName: String,

    @Column("business_license")
    val businessLicense: String? = null,

    @Column("ceo_name")
    val ceoName: String? = null,

    @Column("headquarters_address")
    val headquartersAddress: String? = null,

    @Column("contact_phone")
    val contactPhone: String? = null,

    @Column("website")
    val website: String? = null,

    @Column("registration_date")
    val registrationDate: LocalDate? = null,

    @Column("email")
    val email: String? = null,

    @Column("fax_number")
    val faxNumber: String? = null,

    @Column("description")
    val description: String? = null,

    @Column("is_active")
    val isActive: Boolean = true,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("created_by")
    val createdBy: String? = null,

    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_by")
    val updatedBy: String? = null,

    @Column("deleted_at")
    val deletedAt: LocalDateTime? = null,

    @Column("deleted_by")
    val deletedBy: String? = null,

    @Column("version")
    val version: Long = 0
)