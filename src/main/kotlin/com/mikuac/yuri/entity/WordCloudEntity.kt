package com.mikuac.yuri.entity

import org.hibernate.annotations.Type
import java.time.LocalDate
import javax.persistence.*

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

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(nullable = false)
    val content: String,

    @Column(nullable = false)
    val time: LocalDate,

    )