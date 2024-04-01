package com.mikuac.yuri.entity

import jakarta.persistence.*

@Entity
@Table(name = "steam_player_status")
data class SteamPlayerStatusEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val groupId: Long,

    @Column(nullable = false)
    var steamId: String,

    @Column(nullable = false)
    var nickname: String

)