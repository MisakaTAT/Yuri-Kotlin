package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
data class SauceNaoCacheEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val md5: String,

    @Column(nullable = false, unique = false)
    val infoResult: String

)