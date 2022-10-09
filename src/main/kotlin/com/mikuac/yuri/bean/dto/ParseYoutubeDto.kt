package com.mikuac.yuri.bean.dto

data class ParseYoutubeDto(
    val items: List<Item>,
) {
    data class Item(
        val id: String,
        val snippet: Snippet,
        val statistics: Statistics
    ) {
        data class Snippet(
            val channelTitle: String,
            val publishedAt: String,
            val thumbnails: Thumbnails,
            val title: String
        ) {
            data class Thumbnails(
                val maxres: Maxres,
            ) {
                data class Maxres(
                    val url: String
                )
            }
        }

        data class Statistics(
            val favoriteCount: String,
            val likeCount: String,
            val viewCount: String
        )
    }
}







