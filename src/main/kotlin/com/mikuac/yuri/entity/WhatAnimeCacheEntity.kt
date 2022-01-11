package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
data class WhatAnimeCacheEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val md5: String,

    @Column(nullable = false, unique = false)
    val infoResult: String,

    @Column(nullable = false, unique = false)
    val videoResult: String

)