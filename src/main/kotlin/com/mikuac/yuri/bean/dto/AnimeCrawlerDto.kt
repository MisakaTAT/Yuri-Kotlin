package com.mikuac.yuri.bean.dto

import com.google.gson.annotations.SerializedName

data class AnimeCrawlerDto(
    val code: Int,
    val message: String,
    val result: List<Result>
) {
    data class Result(
        val date: String,
        @SerializedName("date_ts")
        val dateTs: Int,
        @SerializedName("day_of_week")
        val dayOfWeek: Int,
        @SerializedName("is_today")
        val isToday: Int,
        val seasons: List<Season>
    ) {
        data class Season(
            val cover: String,
            val delay: Int,
            @SerializedName("ep_id")
            val epId: Int,
            val favorites: Int,
            val follow: Int,
            @SerializedName("is_published")
            val isPublished: Int,
            @SerializedName("pub_index")
            val pubIndex: String,
            @SerializedName("pub_time")
            val pubTime: String,
            @SerializedName("pub_ts")
            val pubTs: Int,
            @SerializedName("season_id")
            val seasonId: Int,
            @SerializedName("season_status")
            val seasonStatus: Int,
            @SerializedName("square_cover")
            val squareCover: String,
            val title: String,
            val url: String
        )
    }
}



