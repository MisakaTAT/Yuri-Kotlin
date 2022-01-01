package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.GroupBlackListEntity
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
interface GroupBlackListRepository : JpaRepository<GroupBlackListEntity, Int> {

    @Cacheable(cacheNames = ["GroupBlacklistCache"], key = "#groupId")
    fun findByGroupId(groupId: Long): Optional<GroupBlackListEntity>

}