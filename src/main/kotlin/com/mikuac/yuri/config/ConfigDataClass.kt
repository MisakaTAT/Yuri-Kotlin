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
        val telegram: Telegram,
        val blockChain: BlockChain,
        val githubRepo: GithubRepo,
        val phoenixWright: PhoenixWright,
        val parseYoutube: ParseYoutube
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
            val proxy: Boolean,
        )

        data class AnimePic(
            val r18: Boolean,
            val cd: Int,
            val reverseProxy: String,
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

        data class BlockChain(
            val proxy: Boolean
        )

        data class PhoenixWright(
            val proxy: Boolean
        )

        data class GithubRepo(
            val proxy: Boolean
        )

        data class ParseYoutube(
            val proxy: Boolean,
            val apiKey: String
        )

        data class Telegram(
            val enable: Boolean,
            val botUsername: String,
            val botToken: String,
            val proxy: Boolean,
            val rules: Rules,
            val enableUserWhiteList: Boolean,
            val userWhiteList: List<String>
        ) {
            data class Rules(
                val group: List<RuleItem>,
                val channel: List<RuleItem>,
                val friend: List<RuleItem>
            ) {
                data class RuleItem(
                    val source: String,
                    val target: Target,
                ) {
                    data class Target(
                        val group: List<Long>,
                        val friend: List<Long>,
                    )
                }
            }
        }
    }
}
