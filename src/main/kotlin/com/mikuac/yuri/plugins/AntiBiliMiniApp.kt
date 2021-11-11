package com.mikuac.yuri.plugins

import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.dto.BiliMiniAppDto
import com.mikuac.yuri.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AntiBiliMiniApp : BotPlugin() {

    private val regex = Regex("^(.*?)1109937557(.*)")

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private fun request(bid: String): BiliMiniAppDto.Data {
        val result = RequestUtils.get("https://api.bilibili.com/x/web-interface/view?bvid=${bid}")
        return Gson().fromJson(result, BiliMiniAppDto::class.java).data
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            var url = RegexUtils.group(Regex("(?<=\"qqdocurl\":\")(.*)(?=\\?share_medium)"), 1, msg)
            url = url.replace("\\\\".toRegex(), "")
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
        if (!msg.matches(regex)) return
        if (checkUtils.pluginIsDisable(this.javaClass.simpleName)) return
        buildMsg(msg, userId, groupId, bot)
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(event.userId, event.groupId, bot, event.message)
        return MESSAGE_IGNORE
    }

}