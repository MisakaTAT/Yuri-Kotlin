package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.GroupWhiteListEntity
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional

@Component
interface GroupWhiteListRepository : JpaRepository<GroupWhiteListEntity, Int> {

    @Cacheable(cacheNames = ["GroupWhitelistCache"], key = "#groupId")
    fun findByGroupId(groupId: Long): Optional<GroupWhiteListEntity>

    @Transactional
    @CacheEvict(value = ["GroupWhitelistCache"], key = "#groupId")
    fun deleteByGroupId(groupId: Long)

    @Transactional
    @CacheEvict(value = ["GroupWhitelistCache"], key = "#entity.groupId")
    override fun <S : GroupWhiteListEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}