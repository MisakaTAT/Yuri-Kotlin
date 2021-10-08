package com.mikuac.yuri.entity

import javax.persistence.*


@Entity
data class UserBlackListEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val userId: Long

)