package com.mikuac.yuri.dto

class NsfwDTO : ArrayList<NsfwDTO.Item>() {
    data class Item(
        val drawings: Double,
        val hentai: Double,
        val neutral: Double,
        val porn: Double,
        val sexy: Double
    )
}
