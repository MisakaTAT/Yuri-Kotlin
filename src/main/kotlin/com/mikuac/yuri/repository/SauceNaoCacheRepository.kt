package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.SauceNaoCacheEntity
import jakarta.transaction.Transactional
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
interface SauceNaoCacheRepository : JpaRepository<SauceNaoCacheEntity, Int> {

    @Cacheable(cacheNames = ["SauceNaoSearchCache"], key = "#md5")
    fun findByMd5(md5: String): Optional<SauceNaoCacheEntity>

    @Transactional
    @CacheEvict(value = ["SauceNaoSearchCache"], key = "#entity.md5")
    override fun <S : SauceNaoCacheEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}