@file:Suppress("SpellCheckingInspection")

package com.mikuac.yuri.plugins.initiative

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.yuri.dto.AntiBiliMiniAppDTO
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.RegexUtils
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Shiro
@Component
class AntiBiliMiniApp {

    private val expiringMap: ExpiringMap<Long, String> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(300 * 1000L, TimeUnit.MILLISECONDS)
        .build()

    private fun request(bid: String): AntiBiliMiniAppDTO {
        return NetUtils.get("https://api.bilibili.com/x/web-interface/view?bvid=${bid}").use { resp ->
            val data = Gson().fromJson(resp.body?.string(), AntiBiliMiniAppDTO::class.java)
            if (data.code != 0) throw YuriException(data.message)
            data
        }
    }

    private fun parseBidByShortURL(url: String): String {
        return NetUtils.get(url).use { resp ->
            RegexUtils.group("bid", resp.request.url.toString(), Regex.BILIBILI_BID)
        }
    }

    private fun buildMsg(data: AntiBiliMiniAppDTO.Data): String {
        return MsgUtils.builder()
            .img(data.pic)
            .text("\n${ShiroUtils.escape2(data.title)}")
            .text("\nUP：${ShiroUtils.escape2(data.owner.name)}")
            .text("\n播放：${data.stat.view} 弹幕：${data.stat.danmaku}")
            .text("\n投币：${data.stat.coin} 点赞：${data.stat.like}")
            .text("\n评论：${data.stat.reply} 分享：${data.stat.share}")
            .text("\nhttps://www.bilibili.com/video/av${data.stat.aid}")
            .text("\nhttps://www.bilibili.com/video/${data.bvid}")
            .build()
    }

    private fun handleMiniApp(bot: Bot, event: AnyMessageEvent, id: Long) {
        val json = event.arrayMsg.filter { it.type == MsgTypeEnum.json }
        if (json.isNotEmpty()) {
            val jsonObject = JsonParser.parseString(json[0].data["data"])
            val url = jsonObject.asJsonObject["meta"].asJsonObject["detail_1"].asJsonObject["qqdocurl"].asString
            handleRequest(bot, event, id, parseBidByShortURL(url))
        }
    }

    private fun handleURL(bot: Bot, event: AnyMessageEvent, id: Long) {
        handleRequest(bot, event, id, RegexUtils.group("bid", event.message, Regex.BILIBILI_BID))
    }

    private fun handleRequest(bot: Bot, event: AnyMessageEvent, id: Long, bid: String) {
        expiringMap[id]?.let {
            if (it == bid) return
        }
        request(bid).let { resp ->
            bot.sendMsg(event, buildMsg(resp.data), false)
            expiringMap[id] = bid
        }
    }

    @AnyMessageHandler
    fun handler(bot: Bot, event: AnyMessageEvent) {
        ExceptionHandler.with(bot, event) {
            val msg = event.message
            val id = event.groupId ?: event.userId
            if (msg.contains("com.tencent.miniapp_01") && msg.contains("哔哩哔哩")) {
                handleMiniApp(bot, event, id)
            }
            if (msg.contains("bilibili.com/video/BV")) {
                handleURL(bot, event, id)
            }
        }
    }

}
