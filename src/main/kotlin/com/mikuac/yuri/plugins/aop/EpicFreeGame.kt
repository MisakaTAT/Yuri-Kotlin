package com.mikuac.yuri.plugins.aop

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DateUtil
import com.alibaba.fastjson.JSONObject
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.EpicDto
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

    private val expiringMap: ExpiringMap<String, EpicDto> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(ReadConfig.config.plugin.epic.cacheTime.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    private val graphqlQuery =
        "query searchStoreQuery(${'$'}allowCountries: String, ${'$'}category: String, ${'$'}count: Int, ${'$'}country: String!, ${'$'}keywords: String, ${'$'}locale: String, ${'$'}namespace: String, ${'$'}sortBy: String, ${'$'}sortDir: String, ${'$'}start: Int, ${'$'}tag: String, ${'$'}withPrice: Boolean = false, ${'$'}withPromotions: Boolean = false) {\n Catalog {\n searchStore(allowCountries: ${'$'}allowCountries, category: ${'$'}category, count: ${'$'}count, country: ${'$'}country, keywords: ${'$'}keywords, locale: ${'$'}locale, namespace: ${'$'}namespace, sortBy: ${'$'}sortBy, sortDir: ${'$'}sortDir, start: ${'$'}start, tag: ${'$'}tag) {\n elements {\n title\n id\n namespace\n description\n effectiveDate\n keyImages {\n type\n url\n }\n seller {\n id\n name\n }\n productSlug\n urlSlug\n url\n items {\n id\n namespace\n }\n customAttributes {\n key\n value\n }\n price(country: ${'$'}country) @include(if: ${'$'}withPrice) {\n totalPrice {\n discountPrice\n originalPrice\n voucherDiscount\n discount\n currencyCode\n currencyInfo {\n decimals\n }\n fmtPrice(locale: ${'$'}locale) {\n originalPrice\n discountPrice\n intermediatePrice\n }\n }\n lineOffers {\n appliedRules {\n id\n endDate\n discountSetting {\n discountType\n }\n }\n }\n }\n promotions(category: ${'$'}category) @include(if: ${'$'}withPromotions) {\n promotionalOffers {\n promotionalOffers {\n startDate\n endDate\n discountSetting {\n discountType\n discountPercentage\n }\n }\n }\n upcomingPromotionalOffers {\n promotionalOffers {\n startDate\n endDate\n discountSetting { discountType\n discountPercentage\n }\n }\n }\n }\n }\n  }\n }\n}\n"

    private fun doRequest(): EpicDto? {
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

        // 检查缓存
        if (expiringMap["cache"] != null) {
            return expiringMap["cache"]
        }
        val jsonObject = JsonParser.parseString(result)
        val elements = jsonObject.asJsonObject["data"].asJsonObject["Catalog"].asJsonObject["searchStore"]
            .asJsonObject["elements"].asJsonArray ?: throw Exception("Epic 免费游戏获取失败")
        val data = Gson().fromJson(elements, EpicDto::class.java)
        expiringMap["cache"] = data
        return data
    }

    private fun formatDate(rawDate: String): String {
        var date = DateUtil.parse(rawDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        date = DateUtil.offset(date, DateField.HOUR, 8)
        return DateUtil.format(date, "yyy年MM月dd日 HH时mm分")
    }

    private fun buildMsg(userId: Long, groupId: Long, selfId: Long, bot: Bot) {
        try {
            val msgList = ArrayList<String>()
            val games = doRequest() ?: return
            var i = 1
            for (game in games.reversed()) {
                // 取最新的三条
                if (i > 3) break

                val pons = game.promotions.promotionalOffers
                val upon = game.promotions.upcomingPromotionalOffers
                val gameName = game.title
                val gameDesc = game.description
                val gamePrice = game.price.totalPrice.fmtPrice.originalPrice

                val image = game.keyImages.filter {
                    "Thumbnail" == it.type || "VaultClosed" == it.type
                }
                var imageUrl: String? = null
                if (image.isNotEmpty()) imageUrl = image[0].url

                val publisherName: String = game.customAttributes.filter { it.key == "publisherName" }[0].value
                val developerName: String = game.customAttributes.filter { it.key == "developerName" }[0].value

                val gamePage = "https://www.epicgames.com/store/zh-CN/p/${game.urlSlug}"

                val msg = MsgUtils.builder()

                if (upon.isNotEmpty()) {
                    val startDate = formatDate(upon[0].promotionalOffers[0].startDate)
                    val endDate = formatDate(upon[0].promotionalOffers[0].endDate)
                    if (imageUrl != null) msg.img(imageUrl)
                    msg.text("\n即将解锁：$gameName")
                    msg.text("\n限免时间为 $startDate 到 $endDate")
                }

                if (pons.isNotEmpty()) {
                    val startDate = formatDate(pons[0].promotionalOffers[0].startDate)
                    val endDate = formatDate(pons[0].promotionalOffers[0].endDate)
                    if (imageUrl != null) msg.img(imageUrl)
                    msg.text("\n${gameName}（${gamePrice}）")
                    msg.text("\n\n${gameDesc}")
                    msg.text("\n\n该游戏由 $developerName 制作，并由 $publisherName 发行。")
                    msg.text("\n\n限免时间为 $startDate 到 $endDate")
                    msg.text("\n\n感兴趣的小伙伴可以点击下方链接免费领取啦～")
                    msg.text("\n${gamePage}")
                }

                msgList.add(msg.build())
                i++
            }
            val msg = ShiroUtils.generateForwardMsg(selfId, ReadConfig.config.base.botName, msgList)
                ?: throw Exception("合并转发消息创建失败")
            bot.sendGroupForwardMsg(groupId, msg)
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