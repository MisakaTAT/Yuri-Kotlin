package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class WhatAnimeDTO(
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
            val id: Int,
            val season: String,
            val startDate: StartDate,
            val status: String,
            val synonyms: List<String>,
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
                val english: String,
                val native: String,
                val romaji: String
            )
        }
    }
}

