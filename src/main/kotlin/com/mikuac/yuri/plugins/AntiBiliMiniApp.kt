package com.mikuac.yuri.plugins

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.dto.BiliVideoApiDto
import com.mikuac.yuri.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AntiBiliMiniApp : BotPlugin() {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private fun request(bid: String): BiliVideoApiDto.Data {
        val result = RequestUtils.get("https://api.bilibili.com/x/web-interface/view?bvid=${bid}")
        return Gson().fromJson(result, BiliVideoApiDto::class.java).data
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            val jsonString = StringUtils.unescape(msg).replace("[CQ:json,data=", "").replace("]", "")
            val jsonObject = JsonParser.parseString(jsonString)
            val url = jsonObject.asJsonObject["meta"].asJsonObject["detail_1"].asJsonObject["qqdocurl"].asString
            val realUrl = RequestUtils.findLink(url)
            val bid = RegexUtils.group(Regex("(?<=video/)(.*)(?=\\?p=)"), 1, realUrl)
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
            MsgSendUtils.atSend(userId, groupId, bot, "哔哩哔哩小程序解析失败 ${e.message}")
            LogUtils.debug("${DateUtils.getTime()} ${this.javaClass.simpleName} Exception")
            LogUtils.debug(e.stackTraceToString())
        }
    }

    private fun check(userId: Long, groupId: Long, bot: Bot, msg: String) {
        if (!msg.contains("com.tencent.miniapp_01") && !msg.contains("哔哩哔哩")) return
        if (checkUtils.pluginIsDisable(this.javaClass.simpleName)) return
        buildMsg(msg, userId, groupId, bot)
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(event.userId, event.groupId, bot, event.message)
        return MESSAGE_IGNORE
    }

}