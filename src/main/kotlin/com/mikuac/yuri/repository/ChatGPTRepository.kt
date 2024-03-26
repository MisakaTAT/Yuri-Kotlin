package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.ChatGPTEntity
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatGPTRepository : JpaRepository<ChatGPTEntity, Int> {

    @Cacheable(cacheNames = ["ChatGPTCache"], key = "#userId")
    fun findByUserId(userId: Long): Optional<ChatGPTEntity>

    @Transactional
    @CacheEvict(value = ["ChatGPTCache"], key = "#userId")
    fun deleteByUserId(userId: Long)

    @Transactional
    @CacheEvict(value = ["ChatGPTCache"], key = "#entity.userId")
    override fun <S : ChatGPTEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}