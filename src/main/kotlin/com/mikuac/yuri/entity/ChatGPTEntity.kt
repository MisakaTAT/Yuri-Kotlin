package com.mikuac.yuri.entity

import jakarta.persistence.*

@Entity
@Table(name = "chat_gpt")
class ChatGPTEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Lob
    @Column(nullable = false)
    var personality: String,

    @Column(nullable = false, unique = true)
    val userId: Long,

    )