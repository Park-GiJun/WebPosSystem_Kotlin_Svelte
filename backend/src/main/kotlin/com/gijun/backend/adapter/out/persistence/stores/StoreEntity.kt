package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.enums.OperationType
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.PhoneNumber
import com.gijun.backend.domain.store.vo.StoreId
import jdk.dynalink.Operation
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
    val storeId: String, // StoreId -> String으로 변경

    @Column("store_name")
    val storeName: String,

    @Column("store_type")
    val storeType: StoreType,

    @Column("operation_type")
    val operationType: OperationType? = null,

    @Column("hq_id")
    val hqId: String? = null, // HeadquartersId -> String으로 변경

    @Column("region_code")
    val regionCode: String,

    @Column("store_number")
    val storeNumber: String,

    @Column("business_license")
    val businessLicense: String? = null, // BusinessLicense -> String으로 변경

    @Column("owner_name")
    val ownerName: String,

    @Column("phone_number")
    val phoneNumber: String? = null, // PhoneNumber -> String으로 변경

    @Column("address")
    val address: String? = null,

    @Column("postal_code")
    val postalCode: String? = null,

    @Column("opening_date")
    val openingDate: LocalDate = LocalDate.now(),

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