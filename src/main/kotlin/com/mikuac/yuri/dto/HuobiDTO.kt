package com.mikuac.yuri.dto

class HuobiDTO(
    val status: String,
    val ts: String,
    val data: List<Ticker>
) {
    data class Ticker(
        val open: Double,
        val close: Double,
        val low: Double,
        val high: Double,
        val amount: Double,
    )
}