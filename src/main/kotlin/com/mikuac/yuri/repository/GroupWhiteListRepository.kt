package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.GroupWhiteListEntity
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
interface GroupWhiteListRepository : JpaRepository<GroupWhiteListEntity, Int> {

    @Cacheable(cacheNames = ["GroupWhitelistCache"], key = "#groupId")
    fun findByGroupId(groupId: Long): Optional<GroupWhiteListEntity>


}