package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.vo.StoreId
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.PhoneNumber
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.enums.OperationType
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class StoreMapper {
    
    /**
     * Entity를 Domain으로 변환
     */
    fun toDomain(entity: StoreEntity): Store {
        return Store(
            storeId = StoreId(entity.storeId),
            storeName = entity.storeName,
            storeType = parseStoreType(entity.storeType),
            operationType = parseOperationType(entity.operationType),
            hqId = entity.hqId?.let { HeadquartersId(it) },
            regionCode = entity.regionCode ?: "000",
            storeNumber = entity.storeNumber ?: "001",
            businessLicense = entity.businessLicense?.let { BusinessLicense(it) },
            ownerName = entity.ownerName ?: "Unknown",
            phoneNumber = PhoneNumber.fromStringOrNull(entity.phoneNumber ?: entity.contactPhone),
            address = entity.address ?: entity.storeAddress,
            postalCode = entity.postalCode,
            openingDate = entity.openingDate ?: entity.registrationDate ?: LocalDate.now(),
            storeStatus = entity.storeStatus,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
            deletedAt = entity.deletedAt,
            deletedBy = entity.deletedBy
        )
    }

    /**
     * Domain을 Entity로 변환
     */
    fun toEntity(domain: Store): StoreEntity {
        return StoreEntity(
            storeId = domain.storeId.value,
            storeCode = "${domain.regionCode}${domain.storeNumber}",
            storeName = domain.storeName,
            storeType = domain.storeType.name,
            operationType = domain.operationType?.name,
            hqId = domain.hqId?.value,
            regionCode = domain.regionCode,
            storeNumber = domain.storeNumber,
            businessLicense = domain.businessLicense?.value,
            ownerName = domain.ownerName,
            phoneNumber = domain.phoneNumber?.value,
            address = domain.address,
            postalCode = domain.postalCode,
            openingDate = domain.openingDate,
            storeAddress = domain.address, // 호환성을 위해 중복
            contactPhone = domain.phoneNumber?.value, // 호환성을 위해 중복
            storeManagerId = null,
            registrationDate = domain.openingDate, // 호환성을 위해 중복
            businessHours = "09:00-22:00", // 기본값
            storeStatus = domain.storeStatus,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            createdBy = domain.createdBy,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy,
            deletedAt = domain.deletedAt,
            deletedBy = domain.deletedBy,
            version = 0L
        )
    }
    
    /**
     * 문자열을 StoreType으로 변환
     */
    private fun parseStoreType(storeType: String?): StoreType {
        return when (storeType?.uppercase()) {
            "INDIVIDUAL" -> StoreType.INDIVIDUAL
            "CHAIN" -> StoreType.CHAIN
            else -> StoreType.INDIVIDUAL
        }
    }

    /**
     * 문자열을 OperationType으로 변환
     */
    private fun parseOperationType(operationType: String?): OperationType? {
        return when (operationType?.uppercase()) {
            "DIRECT" -> OperationType.DIRECT
            "FRANCHISE" -> OperationType.FRANCHISE
            else -> null
        }
    }

    /**
     * Entity 리스트를 Domain 리스트로 변환
     */
    fun toDomainList(entities: List<StoreEntity>): List<Store> {
        return entities.map { toDomain(it) }
    }

    /**
     * Domain 리스트를 Entity 리스트로 변환
     */
    fun toEntityList(domains: List<Store>): List<StoreEntity> {
        return domains.map { toEntity(it) }
    }
}
