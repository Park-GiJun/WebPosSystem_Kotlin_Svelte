package com.gijun.backend.domain.common

import java.time.LocalDateTime

data class RegionCode(
    val regionCode : String,
    val regionName : String,
    val depth : Int,
    val parentRegion : RegionCode?,
    val isActive : Boolean = true,
    val createdAt : LocalDateTime = LocalDateTime.now(),
    val createdBy : String,
    val updatedAt : LocalDateTime = LocalDateTime.now(),
    val updatedBy : String,
    val deletedAt : LocalDateTime?,
    val deletedBy : String?
)
