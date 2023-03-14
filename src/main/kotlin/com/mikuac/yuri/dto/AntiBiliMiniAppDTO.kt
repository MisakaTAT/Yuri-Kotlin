package com.mikuac.yuri.dto

data class AntiBiliMiniAppDTO(
    val code: Int,
    val data: Data,
    val message: String,
) {
    data class Data(
        val bvid: String,
        val pic: String,
        val title: String,
        val owner: Owner,
        val stat: Stat
    ) {
        data class Owner(
            val name: String,
        )

        data class Stat(
            val aid: Int,
            val view: Int,
            val danmaku: Int,
            val reply: Int,
            val coin: Int,
            val share: Int,
            val like: Int
        )
    }
}
