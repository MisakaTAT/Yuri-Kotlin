package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.WhatAnimeCacheEntity
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WhatAnimeCacheRepository : JpaRepository<WhatAnimeCacheEntity, Int> {

    @Cacheable(cacheNames = ["WhatAnimeSearchCache"], key = "#md5")
    fun findByMd5(md5: String): Optional<WhatAnimeCacheEntity>

    @Transactional
    @CacheEvict(value = ["WhatAnimeSearchCache"], key = "#entity.md5")
    override fun <S : WhatAnimeCacheEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}