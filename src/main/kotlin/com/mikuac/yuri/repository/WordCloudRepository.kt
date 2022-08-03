package com.mikuac.yuri.repository

import com.mikuac.yuri.entity.WordCloudEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.transaction.Transactional

@Component
interface WordCloudRepository : JpaRepository<WordCloudEntity, Int> {

    fun findAllBySenderIdAndGroupIdAndTimeBetween(
        senderId: Long, groupId: Long, start: LocalDate, end: LocalDate
    ): List<WordCloudEntity>

    fun findAllByGroupIdAndTimeBetween(groupId: Long, start: LocalDate, end: LocalDate): List<WordCloudEntity>

    @Transactional
    override fun <S : WordCloudEntity> save(entity: S): S {
        TODO("Not yet implemented")
    }

}