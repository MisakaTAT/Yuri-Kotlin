package com.mikuac.yuri.config

data class Config(
    val base: Base,
    val command: Command,
    val plugin: Plugin
) {
    data class Base(
        val debug: Boolean,
        val adminList: List<Long>,
        val botName: String,
        val botSelfId: Long,
        val searchMode: SearchMode
    ) {
        data class SearchMode(
            val timeout: Int
        )
    }

    data class Command(
        val prefix: String
    )

    data class Plugin(
        val animePic: AnimePic,
        val repeat: Repeat,
        val whatAnime: WhatAnime,
        val sauceNao: SauceNao,
        val tencentNLP: TencentNLP
    ) {
        data class AnimePic(
            val r18: Boolean,
            val cdTime: Int,
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

        data class TencentNLP(
            val secretId: String,
            val secretKey: String
        )
    }
}
