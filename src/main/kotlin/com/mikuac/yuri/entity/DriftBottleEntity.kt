package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
data class DriftBottleEntity(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    val groupId: Long,

    @Column(nullable = false)
    val groupName: String,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val userName: String,

    @Column(nullable = false)
    val content: String,

    @Column(nullable = false)
    var open: Boolean

)