package com.mikuac.yuri.entity

import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "whatanime_cache")
data class WhatAnimeCacheEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val md5: String,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(nullable = false, unique = false)
    val infoResult: String,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(nullable = false, unique = false)
    val videoResult: String

)