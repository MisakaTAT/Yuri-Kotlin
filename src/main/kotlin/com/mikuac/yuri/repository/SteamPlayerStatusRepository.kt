package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.SteamPlayerStatusEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SteamPlayerStatusRepository : JpaRepository<SteamPlayerStatusEntity, Int> {

    fun findByGroupId(groupId: Long): List<SteamPlayerStatusEntity>

    fun findByUserIdAndGroupIdAndSteamId(
        userId: Long,
        groupId: Long,
        steamId: String
    ): Optional<SteamPlayerStatusEntity>

    @Transactional
    fun deleteByUserIdAndGroupId(userId: Long, groupId: Long)

}