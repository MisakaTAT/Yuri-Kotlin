package com.mikuac.yuri.plugins.aop

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DateUtil
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.LogUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RequestUtils
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class EpicFreeGame : BotPlugin() {

    private val expiringMap: ExpiringMap<String, JsonArray> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(ReadConfig.config.plugin.epic.cacheTime.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    private val graphqlQuery =
        "query searchStoreQuery(${'$'}allowCountries: String, ${'$'}category: String, ${'$'}count: Int, ${'$'}country: String!, ${'$'}keywords: String, ${'$'}locale: String, ${'$'}namespace: String, ${'$'}sortBy: String, ${'$'}sortDir: String, ${'$'}start: Int, ${'$'}tag: String, ${'$'}withPrice: Boolean = false, ${'$'}withPromotions: Boolean = false) {\n Catalog {\n searchStore(allowCountries: ${'$'}allowCountries, category: ${'$'}category, count: ${'$'}count, country: ${'$'}country, keywords: ${'$'}keywords, locale: ${'$'}locale, namespace: ${'$'}namespace, sortBy: ${'$'}sortBy, sortDir: ${'$'}sortDir, start: ${'$'}start, tag: ${'$'}tag) {\n elements {\n title\n id\n namespace\n description\n effectiveDate\n keyImages {\n type\n url\n }\n seller {\n id\n name\n }\n productSlug\n urlSlug\n url\n items {\n id\n namespace\n }\n customAttributes {\n key\n value\n }\n price(country: ${'$'}country) @include(if: ${'$'}withPrice) {\n totalPrice {\n discountPrice\n originalPrice\n voucherDiscount\n discount\n currencyCode\n currencyInfo {\n decimals\n }\n fmtPrice(locale: ${'$'}locale) {\n originalPrice\n discountPrice\n intermediatePrice\n }\n }\n lineOffers {\n appliedRules {\n id\n endDate\n discountSetting {\n discountType\n }\n }\n }\n }\n promotions(category: ${'$'}category) @include(if: ${'$'}withPromotions) {\n promotionalOffers {\n promotionalOffers {\n startDate\n endDate\n discountSetting {\n discountType\n discountPercentage\n }\n }\n }\n upcomingPromotionalOffers {\n promotionalOffers {\n startDate\n endDate\n discountSetting { discountType\n discountPercentage\n }\n }\n }\n }\n }\n  }\n }\n}\n"

    private fun doRequest(): JsonArray? {
        val variables = JSONObject()
        variables["allowCountries"] = "CN"
        variables["category"] = "freegames"
        variables["count"] = 1000
        variables["country"] = "CN"
        variables["locale"] = "zh-CN"
        variables["sortBy"] = "effectiveDate"
        variables["sortDir"] = "asc"
        variables["withPrice"] = true
        variables["withPromotions"] = true

        val json = JSONObject()
        json["query"] = graphqlQuery
        json["variables"] = variables

        val headers: HashMap<String, String> = HashMap()
        headers["Referer"] = "https://www.epicgames.com/store/zh-CN/"
        headers["Content-Type"] = "application/json; charset=utf-8"

        val result = RequestUtils.post(
            "https://www.epicgames.com/store/backend/graphql-proxy", json.toJSONString(), headers
        )
        if (expiringMap["cache"] != null) {
            return expiringMap["cache"]
        }
        val jsonObject = JsonParser.parseString(result)
        try {
            val data = jsonObject.asJsonObject["data"].asJsonObject["Catalog"].asJsonObject["searchStore"]
                .asJsonObject["elements"].asJsonArray ?: return null
            expiringMap["cache"] = data
            return data
        } catch (e: Exception) {
            throw Exception("Epic 免费游戏获取失败")
        }
    }

    private fun formatDate(rawDate: String): String {
        var date = DateUtil.parse(rawDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        date = DateUtil.offset(date, DateField.HOUR, 8)
        return DateUtil.format(date, "yyy年MM月dd日 HH时mm分")
    }

    private fun buildMsg(userId: Long, groupId: Long, selfId: Long, bot: Bot) {
        try {
            val msgList = JSONArray()
            val games = doRequest() ?: return
            var i = 1
            for (game in games.reversed()) {
                // 取最新的三条
                if (i > 3) break

                var promotionalOffers = JsonObject()
                val gamePromotions = game.asJsonObject["promotions"].asJsonObject["promotionalOffers"].asJsonArray
                val upcomingPromotions =
                    game.asJsonObject["promotions"].asJsonObject["upcomingPromotionalOffers"].asJsonArray
                if (gamePromotions.size() != 0) promotionalOffers = gamePromotions[0].asJsonObject["promotionalOffers"]
                    .asJsonArray[0].asJsonObject
                if (upcomingPromotions.size() != 0) promotionalOffers = upcomingPromotions[0]
                    .asJsonObject["promotionalOffers"].asJsonArray[0].asJsonObject
                val startDate = formatDate(promotionalOffers.asJsonObject["startDate"].asString)
                val endDate = formatDate(promotionalOffers.asJsonObject["endDate"].asString)

                val gameName = game.asJsonObject["title"].asString
                val gameDesc = game.asJsonObject["description"].asString
                // val gameCorp = game.asJsonObject["seller"].asJsonObject["name"].asString
                val gamePrice = game.asJsonObject["price"].asJsonObject["totalPrice"]
                    .asJsonObject["fmtPrice"].asJsonObject["originalPrice"].asString

                val image = game.asJsonObject["keyImages"].asJsonArray.filter {
                    "Thumbnail" == it.asJsonObject["type"].asString || "VaultClosed" == it.asJsonObject["type"].asString
                }
                var imageUrl = ""
                if (image.isNotEmpty()) imageUrl = image[0].asJsonObject["url"].asString

                val publisherName: String = game.asJsonObject["customAttributes"].asJsonArray.filter {
                    it.asJsonObject["key"].asString == "publisherName"
                }[0].asJsonObject["value"].asString
                val developerName: String = game.asJsonObject["customAttributes"].asJsonArray.filter {
                    it.asJsonObject["key"].asString == "developerName"
                }[0].asJsonObject["value"].asString

                val gamePage = "https://www.epicgames.com/store/zh-CN/p/${game.asJsonObject["urlSlug"].asString}"

                val msg = MsgUtils.builder()

                if (upcomingPromotions.size() != 0) {
                    if (imageUrl.isNotEmpty()) msg.img(imageUrl)
                    msg.text("\n即将解锁：$gameName")
                    msg.text("\n限免时间为 $startDate 到 $endDate")
                }

                if (gamePromotions.size() != 0) {
                    if (imageUrl.isNotEmpty()) msg.img(imageUrl)
                    msg.text("${gameName}（${gamePrice}）")
                    msg.text("\n\n${gameDesc}")
                    msg.text("\n\n该游戏由 $developerName 制作，并由 $publisherName 发行。")
                    msg.text("\n\n限免时间为 $startDate 到 $endDate")
                    msg.text("\n\n感兴趣的小伙伴可以点击下方链接免费领取啦～")
                    msg.text("\n${gamePage}")
                }

                val msgObj = JSONObject()
                val msgDataObj = JSONObject()
                msgObj["type"] = "node"
                msgDataObj["name"] = ReadConfig.config.base.botName
                msgDataObj["uin"] = selfId
                msgDataObj["content"] = msg.build()
                msgObj["data"] = msgDataObj
                msgList.add(msgObj)

                i++
            }
            bot.sendGroupForwardMsg(groupId, msgList)
        } catch (e: Exception) {
            MsgSendUtils.atSend(userId, groupId, bot, "Epic免费游戏获取失败：${e.message}")
            LogUtils.debug("${DateUtils.getTime()} ${this.javaClass.simpleName} Exception")
            LogUtils.debug(e.stackTraceToString())
        }
    }

    private fun handler(bot: Bot, event: GroupMessageEvent) {
        if (event.message.matches(RegexEnum.EPIC.value)) buildMsg(event.userId, event.groupId, event.selfId, bot)
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        handler(bot, event)
        return MESSAGE_IGNORE
    }

}