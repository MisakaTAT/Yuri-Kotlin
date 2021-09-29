package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.BlackListEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
interface BlackListRepository : JpaRepository<BlackListEntity, Int> {

    fun findByUserId(userId: Long): Optional<BlackListEntity>

}