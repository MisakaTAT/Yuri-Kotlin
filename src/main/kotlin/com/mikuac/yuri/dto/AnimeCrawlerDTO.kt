package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class AnimeCrawlerDTO(
    val code: Int,
    val message: String,
    val result: List<Result>
) {
    data class Result(
        @SerializedName("day_of_week")
        val dayOfWeek: Int,
        @SerializedName("is_today")
        val isToday: Int,
        val seasons: List<Season>
    ) {
        data class Season(
            val cover: String,
            val delay: Int,
            @SerializedName("pub_time")
            val pubTime: String,
            val title: String,
        )
    }
}