package com.mikuac.yuri.entity

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class WordCloudEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    val senderId: Long,

    val groupId: Long,

    val content: String,

    val time: LocalDate,

    )