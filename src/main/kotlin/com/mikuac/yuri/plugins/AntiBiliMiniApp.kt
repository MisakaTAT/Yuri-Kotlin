package com.mikuac.yuri.plugins

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.dto.BiliVideoApiDto
import com.mikuac.yuri.utils.LogUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RegexUtils
import com.mikuac.yuri.utils.RequestUtils
import org.springframework.stereotype.Component

@Component
class AntiBiliMiniApp : BotPlugin() {

    private fun request(bid: String): BiliVideoApiDto.Data {
        val result = RequestUtils.get("https://api.bilibili.com/x/web-interface/view?bvid=${bid}")
        return Gson().fromJson(result, BiliVideoApiDto::class.java).data
    }

    private fun buildMsg(msgId: Int, json: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            val jsonObject = JsonParser.parseString(json)
            val url = jsonObject.asJsonObject["meta"].asJsonObject["detail_1"].asJsonObject["qqdocurl"].asString
            val realUrl = RequestUtils.findLink(url)
            val bid = RegexUtils.group(Regex("(?<=video/)(.*)(?=\\?)"), 1, realUrl)
            val data = request(bid)
            val sendMsg = MsgUtils.builder()
                .img(data.pic)
                .text("\n${data.title}")
                .text("\nUP：${data.owner.name}")
                .text("\n播放：${data.stat.view} 弹幕：${data.stat.danmaku}")
                .text("\n投币：${data.stat.coin} 点赞：${data.stat.like}")
                .text("\n评论：${data.stat.reply} 分享：${data.stat.share}")
                .text("\nhttps://www.bilibili.com/video/av${data.stat.aid}")
                .text("\nhttps://www.bilibili.com/video/${data.bvid}")
                .build()
            bot.sendGroupMsg(groupId, sendMsg, false)
        } catch (e: Exception) {
            MsgSendUtils.errorSend(msgId, userId, groupId, bot, "哔哩哔哩小程序解析失败", e.message)
            LogUtils.error(e.stackTraceToString())
        }
    }

    @GroupMessageHandler
    fun handler(bot: Bot, event: GroupMessageEvent) {
        val msg = event.message
        if (!msg.contains("com.tencent.miniapp_01") || !msg.contains("哔哩哔哩")) return
        val json = event.arrayMsg.filter {
            it.type == "json"
        }[0].data["data"] ?: return
        buildMsg(event.messageId, json, event.userId, event.groupId, bot)
    }

}
