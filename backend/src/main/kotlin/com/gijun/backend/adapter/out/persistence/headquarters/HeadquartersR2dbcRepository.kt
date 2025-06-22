package com.gijun.backend.adapter.out.persistence.headquarters

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface HeadquartersR2dbcRepository : CoroutineCrudRepository<HeadquartersEntity, String> {
    
    @Query("""
        INSERT INTO headquarters (hq_id, hq_code, hq_name, business_license, ceo_name, 
                                 headquarters_address, contact_phone, website, registration_date, 
                                 email, fax_number, description, is_active, created_at, 
                                 created_by, updated_at, updated_by, deleted_at, deleted_by, version)
        VALUES (:#{#entity.hqId}, :#{#entity.hqCode}, :#{#entity.hqName}, :#{#entity.businessLicense}, 
                :#{#entity.ceoName}, :#{#entity.headquartersAddress}, :#{#entity.contactPhone}, 
                :#{#entity.website}, :#{#entity.registrationDate}, :#{#entity.email}, 
                :#{#entity.faxNumber}, :#{#entity.description}, :#{#entity.isActive}, 
                :#{#entity.createdAt}, :#{#entity.createdBy}, :#{#entity.updatedAt}, 
                :#{#entity.updatedBy}, :#{#entity.deletedAt}, :#{#entity.deletedBy}, 
                :#{#entity.version})
    """)
    suspend fun insertHeadquarters(entity: HeadquartersEntity): Int

}