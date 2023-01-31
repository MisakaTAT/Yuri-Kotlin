package com.mikuac.yuri.entity

import jakarta.persistence.*

@Entity
@Table(name = "group_blacklist")
data class GroupBlackListEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val groupId: Long

)