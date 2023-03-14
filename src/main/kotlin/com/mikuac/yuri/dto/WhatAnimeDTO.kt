package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

class WhatAnimeDTO {
    data class Basic(
        val error: String,
        val result: List<Result>
    ) {
        data class Result(
            @SerializedName("anilist")
            val aniList: Long,
            val episode: Any,
            val from: Double,
            val to: Double,
            val video: String
        )
    }

    data class Detailed(
        val data: Data
    ) {
        data class Data(
            @SerializedName("Media")
            val media: Media
        ) {
            data class Media(
                val coverImage: CoverImage,
                val endDate: EndDate,
                val episodes: Int,
                val format: String,
                val season: String,
                val startDate: StartDate,
                val status: String,
                val title: Title,
                val type: String
            ) {
                data class CoverImage(
                    val large: String
                )

                data class EndDate(
                    val day: Int,
                    val month: Int,
                    val year: Int
                )

                data class StartDate(
                    val day: Int,
                    val month: Int,
                    val year: Int
                )

                data class Title(
                    val chinese: String,
                    val native: String,
                )
            }
        }
    }
}


