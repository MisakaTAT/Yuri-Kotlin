package com.mikuac.yuri.config

import com.mikuac.yuri.plugins.passive.Roulette

data class Config(
    val base: Base,
    val plugin: Plugin
) {
    data class Base(
        val adminList: List<Long>,
        val botName: String,
        val botSelfId: Long,
        val searchMode: SearchMode,
        val enableGroupOnlyWhiteList: Boolean
    ) {
        data class SearchMode(
            val timeout: Int
        )
    }

    data class Plugin(
        val animePic: AnimePic,
        val repeat: Repeat,
        val whatAnime: WhatAnime,
        val sauceNao: SauceNao,
        val epic: Epic,
        val tarot: Tarot,
        val animeCrawler: AnimeCrawler,
        val picSearch: PicSearch,
        val roulette: Roulette,
        val wordCloud: WordCloud,
        val driftBottle: DriftBottle
    ) {
        data class WordCloud(
            val minFontSize: Int,
            val maxFontSize: Int,
        )

        data class PicSearch(
            val similarityLimit: String,
            val alwaysUseAscii2d: Boolean
        )

        data class AnimePic(
            val r18: Boolean,
            val cdTime: Int,
            val proxy: String,
            val recallMsgPicTime: Int
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
            val cdTime: Int
        )

        data class AnimeCrawler(
            val enableLimiter: Boolean,
            val permitsPerMinute: Int
        )

        data class Roulette(
            val timeout: Int,
            val maxMuteTime: Int
        )

        data class DriftBottle(
            val enableLimiter: Boolean,
            val permitsPerMinute: Int
        )
    }
}
