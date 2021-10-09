package com.mikuac.yuri.plugins

import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.LogUtils
import com.mikuac.yuri.common.utils.RegexUtils
import com.mikuac.yuri.common.utils.RequestUtils
import com.mikuac.yuri.dto.BiliMiniAppDto
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class AntiBiliMiniApp : BotPlugin() {

    private val regex = Regex("^(.*?)1109937557(.*)")

    private val log = KotlinLogging.logger {}

    private fun request(bid: String): BiliMiniAppDto.Data? {
        val api = ReadConfig.config.plugin.antiBiliMiniApp.api
        val result = RequestUtils.get(api + bid) ?: return null
        LogUtils.debug("解析哔哩哔哩小程序（result：${result}）")
        return Gson().fromJson(result, BiliMiniAppDto::class.java).data
    }

    private fun buildMsg(bid: String, groupId: Long, bot: Bot) {
        val data = request(bid) ?: return
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
    }

    private fun action(groupId: Long, bot: Bot, msg: String) {
        var url = RegexUtils.group(Regex("(?<=\"qqdocurl\":\")(.*)(?=\\?share_medium)"), 1, msg)
        url = url.replace("\\\\".toRegex(), "")
        val realUrl = RequestUtils.findLink(url) ?: return
        val bid = RegexUtils.group(Regex("(?<=video/)(.*)(?=\\?p=)"), 1, realUrl)
        buildMsg(bid, groupId, bot)
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            action(event.groupId, bot, msg)
            log.info { "Parse bili mini app - User: ${event.userId} Group: ${event.groupId}" }
        }
        return MESSAGE_IGNORE
    }

}