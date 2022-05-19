package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
data class Ascii2dCacheEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val md5: String,

    @Column(nullable = false, unique = false)
    val infoResult: String

)