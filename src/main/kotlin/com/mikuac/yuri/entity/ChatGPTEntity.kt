package com.mikuac.yuri.entity

import jakarta.persistence.*

@Entity
@Table(name = "chat_gpt")
data class ChatGPTEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, columnDefinition = "longtext")
    var preset: String,

    @Column(nullable = false, unique = true)
    val userId: Long,

    )