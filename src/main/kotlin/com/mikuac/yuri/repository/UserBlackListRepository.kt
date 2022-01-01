package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.UserBlackListEntity
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional

@Component
interface UserBlackListRepository : JpaRepository<UserBlackListEntity, Int> {

    @Cacheable(cacheNames = ["UserBlacklistCache"], key = "#userId")
    fun findByUserId(userId: Long): Optional<UserBlackListEntity>

    @Transactional
    @CacheEvict(value = ["UserBlacklistCache"], key = "#userId")
    fun deleteByUserId(userId: Long)

    @Transactional
    @CacheEvict(value = ["UserBlacklistCache"], key = "#entity.userId")
    override fun <S : UserBlackListEntity?> save(entity: S): S {
        TODO("Not yet implemented")
    }

}