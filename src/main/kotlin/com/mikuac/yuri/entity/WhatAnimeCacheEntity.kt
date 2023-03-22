package com.mikuac.yuri.entity

import jakarta.persistence.*

@Entity
@Table(name = "whatanime_cache")
data class WhatAnimeCacheEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val md5: String,

    @Column(nullable = false, unique = false, columnDefinition = "longtext")
    val infoResult: String,

    @Column(nullable = false, unique = false, columnDefinition = "longtext")
    val videoResult: String

)