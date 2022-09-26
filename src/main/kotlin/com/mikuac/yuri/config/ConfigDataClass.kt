package com.mikuac.yuri.config

data class ConfigDataClass(
    val base: Base,
    val plugins: Plugins
) {
    data class Base(
        val adminList: List<Long>,
        val nickname: String,
        val selfId: Long,
        val enableGroupOnlyWhiteList: Boolean,
        val proxy: Proxy
    ) {
        data class Proxy(
            val enable: Boolean,
            val host: String,
            val port: Int,
            val type: String
        )
    }

    data class Plugins(
        val picSearch: PicSearch,
        val animePic: AnimePic,
        val repeat: Repeat,
        val epic: Epic,
        val tarot: Tarot,
        val animeCrawler: AnimeCrawler,
        val roulette: Roulette,
        val wordCloud: WordCloud,
        val driftBottle: DriftBottle,
        val telegram: Telegram
    ) {
        data class WordCloud(
            val cronTaskRate: Int,
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

        data class Telegram(
            val enable: Boolean,
            val botUsername: String,
            val botToken: String,
            val proxy: Boolean,
            val groupRules: List<Rule>,
            val channelRules: List<Rule>,
            val enableUserWhiteList: Boolean,
            val userWhiteList: List<String>
        ) {
            data class Rule(
                val tg: String,
                val qq: Long,
            )
        }
    }
}
