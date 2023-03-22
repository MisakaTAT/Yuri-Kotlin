package com.mikuac.yuri.plugins.passive

import cn.hutool.core.date.DateField
import cn.hutool.core.date.DateUtil
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.EpicFreeGameDTO
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Shiro
@Component
class EpicFreeGame {

    private val cfg = Config.plugins.epic

    private val expiringMap: ExpiringMap<String, EpicFreeGameDTO> =
        ExpiringMap.builder().variableExpiration().expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(cfg.cacheTime.times(1000L), TimeUnit.MILLISECONDS).build()

    private fun request(): EpicFreeGameDTO {
        // 检查缓存
        val cache = expiringMap["cache"]
        if (cache != null) return cache

        val headers: HashMap<String, String> = HashMap()
        headers["Referer"] = "https://www.epicgames.com/store/zh-CN/"
        headers["Content-Type"] = "application/json; charset=utf-8"
        headers["User-Agent"] =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36"

        val data: EpicFreeGameDTO
        val api =
            "https://store-site-backend-static-ipv4.ak.epicgames.com/freeGamesPromotions?locale=zh-CN&country=CN&allowCountries=CN"
        val resp = NetUtils.get(api, headers)
        val jsonObject = JsonParser.parseString(resp.body?.string())
        resp.close()
        val elements = jsonObject
            .asJsonObject["data"]
            .asJsonObject["Catalog"]
            .asJsonObject["searchStore"]
        data = Gson().fromJson(elements, EpicFreeGameDTO::class.java)
        if (data.elements.isEmpty()) throw YuriException("游戏列表为空")
        expiringMap["cache"] = data
        return data
    }

    private fun formatDate(rawDate: String): String {
        var date = DateUtil.parse(rawDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        date = DateUtil.offset(date, DateField.HOUR, 8)
        return DateUtil.format(date, "yyy年MM月dd日 HH时mm分")
    }

    @Suppress("kotlin:S3776")
    private fun buildMsg(): ArrayList<String> {
        try {
            val games = request().elements
            val msgList = ArrayList<String>()
            for (game in games) {
                val gameName = game.title
                val gameCorp = game.seller.name
                var gameThumbnail = ""
                if (game.keyImages.isNotEmpty()) gameThumbnail = game.keyImages[0].url
                val gamePrice = game.price.totalPrice.fmtPrice.originalPrice
                try {
                    val promotions = game.promotions ?: throw YuriException()
                    val gamePromotions = promotions.promotionalOffers
                    val upcomingPromotions = promotions.upcomingPromotionalOffers
                    if (gamePromotions.isEmpty() && upcomingPromotions.isNotEmpty()) {
                        // Promotion is not active yet, but will be active soon.
                        val promotionData = upcomingPromotions[0].promotionalOffers[0]
                        val startDate = formatDate(promotionData.startDate)
                        val endDate = formatDate(promotionData.endDate)
                        val msg = MsgUtils.builder().img(gameThumbnail)
                            .text("\n$gameName ($gamePrice) 即将在 $startDate 推出免费游玩，预计截止时间为 $endDate，该游戏由 $gameCorp 发行。")
                            .build()
                        msgList.add(msg)
                    } else {
                        val gameDesc = game.description
                        val publisherName = game.customAttributes.filter { it.key == "publisherName" }
                        val publisher = if (publisherName.isNotEmpty()) publisherName[0].value else gameCorp
                        val developerName = game.customAttributes.filter { it.key == "developerName" }
                        val developer = if (developerName.isNotEmpty()) developerName[0].value else gameCorp
                        val endDate = formatDate(game.promotions.promotionalOffers[0].promotionalOffers[0].endDate)
                        val gamePage = "https://store.epicgames.com/fr/p/${game.catalogNs.mappings[0].pageSlug}"

                        val msg = MsgUtils.builder().img(gameThumbnail)
                            .text("\n$gameName ($gamePrice) 当前免费，${endDate}截止。").text("\n\n${gameDesc}")
                            .text("\n\n该游戏由 $developer 制作，并由 $publisher 发行。")
                            .text("\n\n感兴趣的小伙伴可以点击下方链接免费领取啦～").text("\n${gamePage}").build()
                        msgList.add(msg)
                    }
                } catch (e: Exception) {
                    // No discounts for this game
                }
            }
            return msgList
        } catch (e: Exception) {
            e.printStackTrace()
            throw YuriException("数据解析失败")
        }
    }

    @AnyMessageHandler(cmd = RegexCMD.EPIC_FREE_GAME)
    fun handler(bot: Bot, event: AnyMessageEvent) {
        ExceptionHandler.with(bot, event) {
            val msg = ShiroUtils.generateForwardMsg(event.selfId, Config.base.nickname, buildMsg())
            bot.sendForwardMsg(event, msg)
        }
    }

}