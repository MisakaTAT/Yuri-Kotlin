package com.mikuac.yuri.plugins.passive

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DateUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.EpicDto
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.RequestUtils
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Shiro
@Component
class EpicFreeGame {

    private val expiringMap: ExpiringMap<String, EpicDto> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(ReadConfig.config.plugin.epic.cacheTime.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    private val graphqlQuery =
        "query searchStoreQuery(${'$'}allowCountries: String, ${'$'}category: String, ${'$'}count: Int, ${'$'}country: String!, ${'$'}keywords: String, ${'$'}locale: String, ${'$'}namespace: String, ${'$'}sortBy: String, ${'$'}sortDir: String, ${'$'}start: Int, ${'$'}tag: String, ${'$'}withPrice: Boolean = false, ${'$'}withPromotions: Boolean = false) {\n Catalog {\n searchStore(allowCountries: ${'$'}allowCountries, category: ${'$'}category, count: ${'$'}count, country: ${'$'}country, keywords: ${'$'}keywords, locale: ${'$'}locale, namespace: ${'$'}namespace, sortBy: ${'$'}sortBy, sortDir: ${'$'}sortDir, start: ${'$'}start, tag: ${'$'}tag) {\n elements {\n title\n id\n namespace\n description\n effectiveDate\n keyImages {\n type\n url\n }\n seller {\n id\n name\n }\n productSlug\n urlSlug\n url\n items {\n id\n namespace\n }\n customAttributes {\n key\n value\n }\n price(country: ${'$'}country) @include(if: ${'$'}withPrice) {\n totalPrice {\n discountPrice\n originalPrice\n voucherDiscount\n discount\n currencyCode\n currencyInfo {\n decimals\n }\n fmtPrice(locale: ${'$'}locale) {\n originalPrice\n discountPrice\n intermediatePrice\n }\n }\n lineOffers {\n appliedRules {\n id\n endDate\n discountSetting {\n discountType\n }\n }\n }\n }\n promotions(category: ${'$'}category) @include(if: ${'$'}withPromotions) {\n promotionalOffers {\n promotionalOffers {\n startDate\n endDate\n discountSetting {\n discountType\n discountPercentage\n }\n }\n }\n upcomingPromotionalOffers {\n promotionalOffers {\n startDate\n endDate\n discountSetting { discountType\n discountPercentage\n }\n }\n }\n }\n }\n  }\n }\n}\n"

    private fun doRequest(): EpicDto? {
        val variables = JsonObject()
        variables.addProperty("allowCountries", "CN")
        variables.addProperty("category", "freegames")
        variables.addProperty("count", 1000)
        variables.addProperty("country", "CN")
        variables.addProperty("locale", "zh-CN")
        variables.addProperty("sortBy", "effectiveDate")
        variables.addProperty("sortDir", "asc")
        variables.addProperty("withPrice", true)
        variables.addProperty("withPromotions", true)

        val json = JsonObject()
        json.addProperty("query", graphqlQuery)
        json.add("variables", variables)

        val headers: HashMap<String, String> = HashMap()
        headers["Referer"] = "https://www.epicgames.com/store/zh-CN/"
        headers["Content-Type"] = "application/json; charset=utf-8"

        // 检查缓存
        if (expiringMap["cache"] != null) {
            return expiringMap["cache"]
        }

        try {
            val api = "https://www.epicgames.com/store/backend/graphql-proxy"
            val result = RequestUtils.post(api, json.toString(), headers) ?: throw YuriException("EPIC API 请求失败")

            val jsonObject = JsonParser.parseString(result.string())
            val elements = jsonObject.asJsonObject["data"].asJsonObject["Catalog"].asJsonObject["searchStore"]
                .asJsonObject["elements"].asJsonArray
            val data = Gson().fromJson(elements, EpicDto::class.java)
            expiringMap["cache"] = data
            return data
        } catch (e: Exception) {
            throw YuriException("响应数据解析失败")
        }
    }

    private fun formatDate(rawDate: String): String {
        var date = DateUtil.parse(rawDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        date = DateUtil.offset(date, DateField.HOUR, 8)
        return DateUtil.format(date, "yyy年MM月dd日 HH时mm分")
    }

    private fun buildMsg(): ArrayList<String> {
        try {
            val games = doRequest() ?: throw YuriException("免费游戏列表获取失败")
            val msgList = ArrayList<String>()
            for (game in games) {
                val gameName = game.title
                val gameCorp = game.seller.name
                val keyImages = game.keyImages.filter { "Thumbnail" == it.type }
                val vaultClosed = game.keyImages.filter { "VaultClosed" == it.type }
                val gameThumbnail = if (keyImages.isNotEmpty()) keyImages[0].url else vaultClosed[0].url
                val gamePrice = game.price.totalPrice.fmtPrice.originalPrice
                try {
                    val gamePromotions = game.promotions.promotionalOffers
                    val upcomingPromotions = game.promotions.upcomingPromotionalOffers
                    if (gamePromotions.isEmpty() && upcomingPromotions.isNotEmpty()) {
                        // Promotion is not active yet, but will be active soon.
                        val promotionData = upcomingPromotions[0].promotionalOffers[0]
                        val startDate = formatDate(promotionData.startDate)
                        val endDate = formatDate(promotionData.endDate)
                        val msg = MsgUtils.builder()
                            .img(gameThumbnail)
                            .text("$gameName ($gamePrice) 即将在 $startDate 推出免费游玩，预计截止时间为 $endDate，该游戏由 $gameCorp 发行。")
                            .build()
                        msgList.add(msg)
                    } else {
                        val gameDesc = game.description
                        val publisherName = game.customAttributes.filter { it.key == "publisherName" }
                        val publisher = if (publisherName.isNotEmpty()) publisherName[0].value else gameCorp
                        val developerName = game.customAttributes.filter { it.key == "developerName" }
                        val developer = if (developerName.isNotEmpty()) developerName[0].value else gameCorp
                        val endDate = formatDate(game.promotions.promotionalOffers[0].promotionalOffers[0].endDate)
                        val gamePage = "https://www.epicgames.com/store/zh-CN/p/${game.urlSlug}"

                        val msg = MsgUtils.builder()
                            .img(gameThumbnail)
                            .text("\n$gameName ($gamePrice) 当前免费，${endDate}截止。")
                            .text("\n\n${gameDesc}")
                            .text("\n\n该游戏由 $developer 制作，并由 $publisher 发行。")
                            .text("\n\n感兴趣的小伙伴可以点击下方链接免费领取啦～")
                            .text("\n${gamePage}")
                            .build()
                        msgList.add(msg)
                    }
                } catch (e: Exception) {
                    // No discounts for this game
                }
            }
            return msgList
        } catch (e: Exception) {
            throw YuriException("EPIC 免费游戏获取失败")
        }
    }

    @GroupMessageHandler(cmd = RegexCMD.EPIC_FREE_GAME)
    fun epicFreeGameHandler(bot: Bot, event: GroupMessageEvent) {
        try {
            val msgList = buildMsg()
            val msg = ShiroUtils.generateForwardMsg(event.selfId, ReadConfig.config.base.botName, msgList)
                ?: throw YuriException("合并转发消息生成失败")
            bot.sendGroupForwardMsg(event.groupId, msg)
        } catch (e: YuriException) {
            bot.sendGroupMsg(event.groupId, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}