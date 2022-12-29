package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class WhatAnimeBasicDTO(
    val error: String,
    val frameCount: Int,
    val result: List<Result>
) {
    data class Result(
        @SerializedName("anilist")
        val aniList: Long,
        val episode: Any,
        val filename: String,
        val from: Double,
        val image: String,
        val similarity: Double,
        val to: Double,
        val video: String
    )
}

