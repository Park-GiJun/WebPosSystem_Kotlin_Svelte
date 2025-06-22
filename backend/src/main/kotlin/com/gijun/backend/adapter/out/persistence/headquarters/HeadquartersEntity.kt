package com.gijun.backend.adapter.out.persistence.headquarters

import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.HeadquartersId
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("headquarters")
data class HeadquartersEntity(
    @Id
    val hqId: HeadquartersId,

    @Column("hq_code")
    val hqCode: String,

    @Column("hq_name")
    val hqName: String,

    @Column("business_license")
    val businessLicense: BusinessLicense? = null,

    @Column("ceo_name")
    val ceoName: String? = null,


)