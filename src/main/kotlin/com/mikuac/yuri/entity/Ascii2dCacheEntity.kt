package com.mikuac.yuri.entity

import jakarta.persistence.*

@Entity
@Table(name = "ascii2d_cache")
data class Ascii2dCacheEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val md5: String,

    @Column(nullable = false, unique = false, columnDefinition = "longtext")
    val infoResult: String

)