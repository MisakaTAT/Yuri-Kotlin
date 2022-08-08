package com.mikuac.yuri.config

data class ConfigDataClass(
    val base: Base,
    val plugins: Plugins
) {
    data class Base(
        val adminList: List<Long>,
        val nickname: String,
        val selfId: Long,
        val enableGroupOnlyWhiteList: Boolean
    )

    data class Plugins(
        val picSearch: PicSearch,
        val animePic: AnimePic,
        val repeat: Repeat,
        val epic: Epic,
        val tarot: Tarot,
        val animeCrawler: AnimeCrawler,
        val roulette: Roulette,
        val wordCloud: WordCloud,
        val driftBottle: DriftBottle
    ) {
        data class WordCloud(
            val minFontSize: Int,
            val maxFontSize: Int,
            val filterRule: List<String>,
        )

        data class PicSearch(
            val timeout: Int,
            val similarity: String,
            val alwaysUseAscii2d: Boolean,
            val animePreviewVideo: Boolean,
            val sauceNaoKey: String,
        )

        data class AnimePic(
            val r18: Boolean,
            val cd: Int,
            val proxy: String,
            val recallPicTime: Int
        )

        data class Repeat(
            val waitTime: Int,
            val thresholdValue: Int
        )

        data class WhatAnime(
            val sendPreviewVideo: Boolean
        )

        data class SauceNao(
            val key: String
        )

        data class Epic(
            val cacheTime: Int
        )

        data class Tarot(
            val cd: Int
        )

        data class AnimeCrawler(
            val rateLimiter: Boolean,
            val permitsPerMinute: Int
        )

        data class Roulette(
            val timeout: Int,
            val maxMuteTime: Int
        )

        data class DriftBottle(
            val cd: Int
        )
    }
}
