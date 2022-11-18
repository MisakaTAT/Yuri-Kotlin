package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
@Table(name = "user_blacklist")
data class UserBlackListEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val userId: Long

)