package com.mikuac.yuri.dto

class BangumiDto : ArrayList<BangumiDto.BangumiDtoItem>() {

    data class BangumiDtoItem(
        val items: List<Item>,
        val weekday: Weekday
    ) {
        data class Item(
            val air_date: String,
            val air_weekday: Int,
            val id: Int,
            val images: Images,
            val name: String,
            val name_cn: String,
            val summary: String,
            val type: Int,
            val url: String
        ) {
            data class Images(
                val common: String,
                val grid: String,
                val large: String,
                val medium: String,
                val small: String
            )
        }

        data class Weekday(
            val cn: String,
            val en: String,
            val id: Int,
            val ja: String
        )
    }

}