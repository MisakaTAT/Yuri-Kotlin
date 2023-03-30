package com.mikuac.yuri.config

data class ConfigDataClass(
    val base: Base, val plugins: Plugins
) {
    data class Base(
        val adminList: List<Long>,
        val nickname: String,
        val selfId: Long,
        val enableGroupOnlyWhiteList: Boolean,
        val proxy: Proxy,
        val mysql: MySQL
    ) {
        data class Proxy(
            val enable: Boolean, val host: String, val port: Int, val type: String
        )

        data class MySQL(
            val url: String, val database: String, val username: String, val password: String
        )
    }

    data class Plugins(
        val picSearch: PicSearch,
        val setu: SeTu,
        val repeat: Repeat,
        val epic: Epic,
        val tarot: Tarot,
        val roulette: Roulette,
        val wordCloud: WordCloud,
        val driftBottle: DriftBottle,
        val telegram: Telegram,
        val huobi: Huobi,
        val githubRepo: GithubRepo,
        val phoenixWright: PhoenixWright,
        val parseYoutube: ParseYoutube,
        val chatGPT: ChatGPT,
        val sendLike: SendLike,
        val webScreenshot: WebScreenshot,
        val rss: Rss
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

        data class SeTu(
            val r18: Boolean,
            val cd: Int,
            val reverseProxy: String,
            val recallPicTime: Int,
            val antiShielding: Int,
        )

        data class Repeat(
            val waitTime: Int, val thresholdValue: Int
        )

        data class Epic(
            val cacheTime: Int
        )

        data class Tarot(
            val cd: Int
        )

        data class Roulette(
            val timeout: Int, val maxMuteTime: Int
        )

        data class DriftBottle(
            val cd: Int
        )

        data class Huobi(
            val proxy: Boolean
        )

        data class PhoenixWright(
            val proxy: Boolean
        )

        data class GithubRepo(
            val proxy: Boolean
        )

        data class ParseYoutube(
            val proxy: Boolean, val apiKey: String
        )

        data class ChatGPT(
            val timeout: Int, val token: String, val messages: List<ChatMessage>, val model: String, val proxy: Boolean
        ) {
            data class ChatMessage(
                val role: String,
                val content: String,
            )
        }

        data class WebScreenshot(
            val proxy: Boolean
        )

        data class SendLike(
            val maxTimes: Int
        )

        data class Rss(
            val urls: List<String>,
            val groups: List<Long>,
            val check: Int
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
                val group: List<RuleItem>, val channel: List<RuleItem>, val friend: List<RuleItem>
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
