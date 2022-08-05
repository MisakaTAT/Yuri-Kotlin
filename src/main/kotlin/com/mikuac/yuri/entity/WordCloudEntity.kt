package com.mikuac.yuri.entity

import java.time.LocalDate
import javax.persistence.*

@Entity
class WordCloudEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    val senderId: Long,

    @Column(nullable = false)
    val groupId: Long,

    @Column(nullable = false)
    val content: String,

    @Column(nullable = false)
    val time: LocalDate,

    )