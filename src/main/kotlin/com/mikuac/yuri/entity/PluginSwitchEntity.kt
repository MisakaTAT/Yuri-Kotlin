package com.mikuac.yuri.entity

import javax.persistence.*

@Entity
@Table(name = "plugin_switch")
data class PluginSwitchEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false, unique = true)
    val pluginName: String,

    @Column(nullable = false)
    val disable: Boolean

)
