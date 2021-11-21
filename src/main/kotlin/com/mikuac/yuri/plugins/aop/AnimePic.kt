package com.mikuac.yuri.plugins.aop

import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.AnimePicDto
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.DateUtils
import com.mikuac.yuri.utils.LogUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RequestUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class AnimePic : BotPlugin() {

    private val log = KotlinLogging.logger {}

    private val expiringMap: ExpiringMap<Long, Long> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(ReadConfig.config.plugin.animePic.cdTime.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    private fun request(r18: Boolean): AnimePicDto.Data {
        var api = "https://api.lolicon.app/setu/v2"
        if (r18) api = "$api?r18=1"
        val result = RequestUtils.get(api)
        val json = Gson().fromJson(result, AnimePicDto::class.java)
        return json.data[0]
    }

    private fun buildTextMsg(r18: Boolean): Pair<String, String?> {
        val data = request(r18)
        return Pair(
            MsgUtils.builder()
                .text("标题：${data.title}")
                .text("\nPID：${data.pid}")
                .text("\n作者：${data.author}")
                .text("\n链接：https://www.pixiv.net/artworks/${data.pid}")
                .text("\n反代链接：${data.urls.original}")
                .build(),
            data.urls.original
        )
    }

    private fun buildPicMsg(url: String?): String {
        url ?: return "图片获取失败了呢 _(:3 」∠)_"
        return MsgUtils.builder().img(url).build()
    }

    private fun handler(msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.EROTIC_PIC.value)) return
        // 检查是否处于冷却时间
        if (expiringMap[groupId + userId] != null && expiringMap[groupId + userId] == userId) {
            val expectedExpiration = expiringMap.getExpectedExpiration(groupId + userId) / 1000
            MsgSendUtils.atSend(userId, groupId, bot, "整天色图色图，信不信把你变成色图？冷却：[${expectedExpiration}秒]")
            return
        }
        buildMsg(msg, userId, groupId, bot)
    }

    private fun recallMsgPic(msgId: Int, bot: Bot) = runBlocking {
        launch {
            delay(ReadConfig.config.plugin.animePic.recallMsgPicTime.times(1000L))
            bot.deleteMsg(msgId)
            log.info { "Recall erotic pic msg img - MsgID: $msgId" }
        }
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        val r18 = msg.contains(Regex("(?i)r18"))
        if (!ReadConfig.config.plugin.animePic.r18 && r18) {
            MsgSendUtils.atSend(userId, groupId, bot, "NSFW禁止！")
            return
        }
        try {
            val buildTextMsg = buildTextMsg(r18)
            MsgSendUtils.send(userId, groupId, bot, buildTextMsg.first)
            val cdTime = ReadConfig.config.plugin.animePic.cdTime.times(1000L)
            expiringMap.put(groupId + userId, userId, cdTime, TimeUnit.MILLISECONDS)
            val msgId = MsgSendUtils.send(userId, groupId, bot, buildPicMsg(buildTextMsg.second))
            recallMsgPic(msgId, bot)
        } catch (e: Exception) {
            MsgSendUtils.atSend(userId, groupId, bot, "色图请求失败 ${e.message}")
            LogUtils.debug("${DateUtils.getTime()} ${this.javaClass.simpleName} Exception")
            LogUtils.debug(e.stackTraceToString())
        }
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        handler(event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        handler(event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

}