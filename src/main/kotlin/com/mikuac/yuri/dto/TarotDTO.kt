package com.mikuac.yuri.dto

data class TarotDTO(
    val tarot: List<Tarot>,
) {
    data class Tarot(
        val name: String,
        val positive: String,
        val negative: String,
        val imageName: String
    )
}

