package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.UserBlackListEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
interface UserBlackListRepository : JpaRepository<UserBlackListEntity, Int> {

    fun findByUserId(userId: Long): Optional<UserBlackListEntity>

}