package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.WhatAnimeCacheEntity
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*
import javax.transaction.Transactional

@Component
interface WhatAnimeCacheRepository : JpaRepository<WhatAnimeCacheEntity, Int> {

    @Cacheable(cacheNames = ["WhatAnimeSearchCache"], key = "#md5")
    fun findByMd5(md5: String): Optional<WhatAnimeCacheEntity>

    @Transactional
    @CacheEvict(value = ["WhatAnimeSearchCache"], key = "#entity.md5")
    override fun <S : WhatAnimeCacheEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}