package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class BiliVideoApiDTO(
    val code: Int,
    val data: Data,
    val message: String,
    val ttl: Int
) {
    data class Data(
        val bvid: String,
        val aid: Int,
        val pic: String,
        val title: String,
        val owner: Owner,
        val stat: Stat
    ) {
        data class Owner(
            val mid: Long,
            val name: String,
            val face: String
        )

        data class Stat(
            val aid: Int,
            val view: Int,
            val danmaku: Int,
            val reply: Int,
            val favorite: Int,
            val coin: Int,
            val share: Int,
            @SerializedName("now_rank")
            val nowRank: Int,
            @SerializedName("his_rank")
            val hisRank: Int,
            val like: Int
        )
    }
}

