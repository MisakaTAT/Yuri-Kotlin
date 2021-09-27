package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
data class PluginSwitchEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    val pluginName: String,

    @Column(nullable = false)
    val disable: Boolean

)
