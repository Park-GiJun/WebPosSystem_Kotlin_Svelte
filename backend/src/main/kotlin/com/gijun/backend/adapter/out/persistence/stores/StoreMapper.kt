package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.vo.StoreId
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.PhoneNumber
import org.springframework.stereotype.Component

@Component
class StoreMapper {
    
    /**
     * Entity를 Domain으로 변환
     */
    fun toDomain(entity: StoreEntity): Store {
        return Store(
            storeId = StoreId(entity.storeId),
            storeName = entity.storeName,
            storeType = entity.storeType,
            operationType = entity.operationType,
            hqId = entity.hqId?.takeIf { it.isNotBlank() }?.let { HeadquartersId(it) },
            regionCode = entity.regionCode,
            storeNumber = entity.storeNumber,
            businessLicense = BusinessLicense.fromStringOrNull(entity.businessLicense),
            ownerName = entity.ownerName,
            phoneNumber = PhoneNumber.fromStringOrNull(entity.phoneNumber),
            address = entity.address,
            postalCode = entity.postalCode,
            openingDate = entity.openingDate,
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
            storeName = domain.storeName,
            storeType = domain.storeType,
            operationType = domain.operationType,
            hqId = domain.hqId?.value,
            regionCode = domain.regionCode,
            storeNumber = domain.storeNumber,
            businessLicense = domain.businessLicense?.value,
            ownerName = domain.ownerName,
            phoneNumber = domain.phoneNumber?.value,
            address = domain.address,
            postalCode = domain.postalCode,
            openingDate = domain.openingDate,
            storeStatus = domain.storeStatus,
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            createdBy = domain.createdBy,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy,
            deletedAt = domain.deletedAt,
            deletedBy = domain.deletedBy,
            version = 0L // 새로 생성시 기본값
        )
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