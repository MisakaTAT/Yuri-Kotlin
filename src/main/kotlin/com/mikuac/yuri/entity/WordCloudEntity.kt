package com.mikuac.yuri.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "word_cloud")
class WordCloudEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    val senderId: Long,

    @Column(nullable = false)
    val groupId: Long,

    @Column(nullable = false, columnDefinition = "longtext")
    val content: String,

    @Column(nullable = false)
    val time: LocalDate,

    )