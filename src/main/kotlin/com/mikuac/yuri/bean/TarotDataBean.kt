package com.mikuac.yuri.bean

data class TarotDataBean(
    val tarot: List<Tarot>,
) {
    data class Tarot(
        val name: String,
        val positive: String,
        val negative: String,
        val imageName: String
    )
}
