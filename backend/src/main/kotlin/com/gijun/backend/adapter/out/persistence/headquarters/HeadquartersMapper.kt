package com.gijun.backend.adapter.out.persistence.headquarters

import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.enums.StoreStatus
import com.gijun.backend.domain.store.enums.StoreType
import com.gijun.backend.domain.store.vo.BusinessLicense
import com.gijun.backend.domain.store.vo.HeadquartersId
import com.gijun.backend.domain.store.vo.PhoneNumber
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class HeadquartersMapper {
    fun toDomain(entity: HeadquartersEntity): Headquarters {
        return Headquarters(
            hqId = HeadquartersId.fromString(entity.hqId),
            hqCode = entity.hqCode,
            hqName = entity.hqName,
            businessLicense = entity.businessLicense?.let { BusinessLicense(it) },
            ceoName = entity.ceoName,
            headquartersAddress = entity.headquartersAddress,
            contactPhone = entity.contactPhone?.let { PhoneNumber(it) },
            website = entity.website,
            storeType = StoreType.FRANCHISE, // 기본값
            hqStatus = StoreStatus.ACTIVE, // 기본값
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
            deletedAt = entity.deletedAt,
            deletedBy = entity.deletedBy
        )
    }

    fun toEntity(domain: Headquarters): HeadquartersEntity {
        return HeadquartersEntity(
            hqId = domain.hqId.value,
            hqCode = domain.hqCode,
            hqName = domain.hqName,
            businessLicense = domain.businessLicense?.value,
            ceoName = domain.ceoName,
            headquartersAddress = domain.headquartersAddress,
            contactPhone = domain.contactPhone?.value,
            website = domain.website,
            registrationDate = LocalDate.now(), // 기본값
            email = null, // 도메인 모델에 없음
            faxNumber = null, // 도메인 모델에 없음
            description = null, // 도메인 모델에 없음
            isActive = domain.isActive,
            createdAt = domain.createdAt,
            createdBy = domain.createdBy,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy,
            deletedAt = domain.deletedAt,
            deletedBy = domain.deletedBy,
            version = 0
        )
    }
}