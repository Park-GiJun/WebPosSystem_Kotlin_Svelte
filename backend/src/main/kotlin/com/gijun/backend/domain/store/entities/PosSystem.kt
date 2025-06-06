package com.gijun.backend.domain.store.entities

import com.gijun.backend.domain.store.vo.*
import com.gijun.backend.domain.store.enums.*
import com.gijun.backend.domain.common.AuditableEntity
import java.time.LocalDate
import java.time.LocalDateTime

data class PosSystem(
    val posId: PosId,
    val storeId: StoreId,
    val posNumber: Int,
    val posName: String? = null,
    val posType: PosType = PosType.MAIN,
    val ipAddress: String? = null,
    val macAddress: String? = null,
    val serialNumber: String? = null,
    val installedDate: LocalDate = LocalDate.now(),
    val lastMaintenanceDate: LocalDate = LocalDate.now(),
    val posStatus: PosStatus = PosStatus.ACTIVE,
    override val isActive: Boolean = true,
    override val createdAt: LocalDateTime = LocalDateTime.now(),
    override val createdBy: String? = null,
    override val updatedAt: LocalDateTime = LocalDateTime.now(),
    override val updatedBy: String? = null,
    override val deletedAt: LocalDateTime? = null,
    override val deletedBy: String? = null
) : AuditableEntity {

    init {
        require(posNumber > 0) { "POS 번호는 1 이상이어야 합니다." }
    }

    fun isMainPos(): Boolean = posType.isMain()
    fun isUnderMaintenance(): Boolean = posStatus.needsMaintenance()

    fun delete(deletedBy: String): PosSystem =
        this.copy(
            isActive = false,
            posStatus = PosStatus.INACTIVE,
            deletedAt = LocalDateTime.now(),
            deletedBy = deletedBy,
            updatedAt = LocalDateTime.now(),
            updatedBy = deletedBy
        )

    fun changeStatus(newStatus: PosStatus, updatedBy: String): PosSystem =
        this.copy(
            posStatus = newStatus,
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    fun performMaintenance(updatedBy: String): PosSystem =
        this.copy(
            posStatus = PosStatus.MAINTENANCE,
            lastMaintenanceDate = LocalDate.now(),
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    fun completeMaintenance(updatedBy: String): PosSystem =
        this.copy(
            posStatus = PosStatus.ACTIVE,
            lastMaintenanceDate = LocalDate.now(),
            updatedAt = LocalDateTime.now(),
            updatedBy = updatedBy
        )

    fun updateHardwareInfo(
        ipAddress: String? = null,
        macAddress: String? = null,
        serialNumber: String? = null,
        updatedBy: String
    ): PosSystem = this.copy(
        ipAddress = ipAddress ?: this.ipAddress,
        macAddress = macAddress ?: this.macAddress,
        serialNumber = serialNumber ?: this.serialNumber,
        updatedAt = LocalDateTime.now(),
        updatedBy = updatedBy
    )

    companion object {
        fun create(
            storeId: StoreId,
            posNumber: Int,
            posType: PosType = PosType.MAIN,
            posName: String? = null,
            createdBy: String,
            ipAddress: String? = null,
            macAddress: String? = null,
            serialNumber: String? = null
        ): PosSystem {
            return PosSystem(
                posId = PosId.generate(storeId, posNumber),
                storeId = storeId,
                posNumber = posNumber,
                posName = posName ?: "POS $posNumber",
                posType = posType,
                ipAddress = ipAddress,
                macAddress = macAddress,
                serialNumber = serialNumber,
                createdBy = createdBy
            )
        }
    }
}