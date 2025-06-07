package com.gijun.backend.configuration

import com.gijun.backend.domain.permission.enums.PermissionType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresPermission(
    val menuCode: String,
    val permission: PermissionType = PermissionType.READ
)