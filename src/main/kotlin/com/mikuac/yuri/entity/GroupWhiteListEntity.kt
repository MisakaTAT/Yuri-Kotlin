package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
data class GroupWhiteListEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val groupId: Long

)