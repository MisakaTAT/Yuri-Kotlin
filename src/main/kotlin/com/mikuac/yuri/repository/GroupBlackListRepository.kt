package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.GroupBlackListEntity
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
interface GroupBlackListRepository : JpaRepository<GroupBlackListEntity, Int> {

    @Cacheable(cacheNames = ["GroupBlacklistCache"], key = "#groupId")
    fun findByGroupId(groupId: Long): Optional<GroupBlackListEntity>

    @Transactional
    @CacheEvict(value = ["GroupBlacklistCache"], key = "#groupId")
    fun deleteByGroupId(groupId: Long)

    @Transactional
    @CacheEvict(value = ["GroupBlacklistCache"], key = "#entity.groupId")
    override fun <S : GroupBlackListEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}