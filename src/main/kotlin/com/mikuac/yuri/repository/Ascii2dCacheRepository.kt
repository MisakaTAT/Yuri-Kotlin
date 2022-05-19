package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.Ascii2dCacheEntity
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional

@Component
interface Ascii2dCacheRepository : JpaRepository<Ascii2dCacheEntity, Int> {

    @Cacheable(cacheNames = ["Ascii2dSearchCache"], key = "#md5")
    fun findByMd5(md5: String): Optional<Ascii2dCacheEntity>

    @Transactional
    @CacheEvict(value = ["Ascii2dSearchCache"], key = "#entity.md5")
    override fun <S : Ascii2dCacheEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}