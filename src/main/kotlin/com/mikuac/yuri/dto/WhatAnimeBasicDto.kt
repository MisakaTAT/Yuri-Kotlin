package com.mikuac.yuri.dto

data class WhatAnimeBasicDto(
    val error: String,
    val frameCount: Int,
    val result: List<Result>
) {
    data class Result(
        val anilist: Long,
        val episode: Int,
        val filename: String,
        val from: Double,
        val image: String,
        val similarity: Double,
        val to: Double,
        val video: String
    )
}

