package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.enums.StoreStatus
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("stores")
data class StoreEntity(
    @Id
    @Column("store_id")
    val storeId: String,

    @Column("store_code")
    val storeCode: String,

    @Column("store_name")
    val storeName: String,

    @Column("store_type")
    val storeType: String? = null,

    @Column("operation_type")
    val operationType: String? = null,

    @Column("hq_id")
    val hqId: String? = null,

    @Column("region_code")
    val regionCode: String? = null,

    @Column("store_number")
    val storeNumber: String? = null,

    @Column("business_license")
    val businessLicense: String? = null,

    @Column("owner_name")
    val ownerName: String? = null,

    @Column("phone_number")
    val phoneNumber: String? = null,

    @Column("address")
    val address: String? = null,

    @Column("postal_code")
    val postalCode: String? = null,

    @Column("opening_date")
    val openingDate: LocalDate? = null,

    @Column("store_address")
    val storeAddress: String? = null,

    @Column("contact_phone")
    val contactPhone: String? = null,

    @Column("store_manager_id")
    val storeManagerId: String? = null,

    @Column("registration_date")
    val registrationDate: LocalDate? = null,

    @Column("business_hours")
    val businessHours: String? = null,

    @Column("store_status")
    val storeStatus: StoreStatus = StoreStatus.ACTIVE,

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

    @Version
    val version: Long = 0
)
