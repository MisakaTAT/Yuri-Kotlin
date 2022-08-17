package com.mikuac.yuri.bean.dto

class NsfwDto : ArrayList<NsfwDto.Item>() {
    data class Item(
        val drawings: Double,
        val hentai: Double,
        val neutral: Double,
        val porn: Double,
        val sexy: Double
    )
}