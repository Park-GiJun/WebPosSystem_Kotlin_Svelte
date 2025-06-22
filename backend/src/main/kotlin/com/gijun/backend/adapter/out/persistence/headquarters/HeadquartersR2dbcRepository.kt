package com.gijun.backend.adapter.out.persistence.headquarters

import com.gijun.backend.domain.store.entities.Headquarters
import com.gijun.backend.domain.store.vo.HeadquartersId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface HeadquartersR2dbcRepository : CoroutineCrudRepository<Headquarters, HeadquartersId> {

}