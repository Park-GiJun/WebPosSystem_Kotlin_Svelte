package com.gijun.backend.adapter.out.persistence.stores

import com.gijun.backend.domain.store.entities.Store
import com.gijun.backend.domain.store.vo.StoreId
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.PhoneNumber
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
            operationType = null, // 현재 테이블에 없음
            hqId = entity.hqId?.let { HeadquartersId(it) },
            regionCode = entity.storeCode, // store_code를 region_code로 매핑
            storeNumber = "001", // 기본값
            businessLicense = null, // 현재 테이블에 없음
            ownerName = "Unknown", // 현재 테이블에 없음 - store_manager_id로 조회 필요
            phoneNumber = PhoneNumber.fromStringOrNull(entity.contactPhone),
            address = entity.storeAddress,
            postalCode = null, // 현재 테이블에 없음
            openingDate = entity.registrationDate ?: LocalDate.now(),
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
            storeCode = domain.regionCode, // region_code를 store_code로 매핑
            storeName = domain.storeName,
            hqId = domain.hqId?.value,
            storeAddress = domain.address,
            contactPhone = domain.phoneNumber?.value,
            storeManagerId = null, // 추후 설정
            registrationDate = domain.openingDate,
            businessHours = "09:00-22:00", // 기본값
            storeType = domain.storeType.name,
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
    private fun parseStoreType(storeType: String?): com.gijun.backend.domain.store.enums.StoreType {
        return when (storeType?.uppercase()) {
            "INDIVIDUAL" -> com.gijun.backend.domain.store.enums.StoreType.INDIVIDUAL
            "CHAIN" -> com.gijun.backend.domain.store.enums.StoreType.CHAIN
            else -> com.gijun.backend.domain.store.enums.StoreType.INDIVIDUAL
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