package com.mikuac.yuri.dto

class BlockChainDTO(
    val ch: String,
    val status: String,
    val ts: String,
    val data: List<Ticker>
) {
    data class Ticker(
        val id: String,
        val open: Double,
        val close: Double,
        val low: Double,
        val high: Double,
        val amount: Double,
        val vol: String,
        val count: Int
    )
}