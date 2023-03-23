package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class DouYinParseDTO(

    @SerializedName("aweme_detail")
    val detail: Detail,

    ) {
    data class Detail(
        val desc: String,
        val video: Video,
    ) {
        data class Video(
            @SerializedName("play_addr")
            val play: Play,
            val cover: Cover,
        ) {
            data class Cover(
                @SerializedName("url_list")
                val urls: List<String>
            )

            data class Play(
                @SerializedName("url_list")
                val urls: List<String>
            )
        }
    }
}
