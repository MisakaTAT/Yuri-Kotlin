package com.mikuac.yuri.config

import com.mikuac.yuri.plugins.passive.AnimeCrawler

data class Config(
    val base: Base,
    val plugin: Plugin
) {
    data class Base(
        val adminList: List<Long>,
        val botName: String,
        val botSelfId: Long,
        val searchMode: SearchMode
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
        val animeCrawler: AnimeCrawler
    ) {
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
            val enable: Boolean,
            val permitsPerMinute: Int
        )
    }
}
