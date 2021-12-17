package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class PhoenixWright : BotPlugin() {

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        val regex = RegexEnum.PHOENIX_WRIGHT.value
        if (!msg.matches(regex)) return
        val topText = URLEncoder.encode(RegexUtils.group(regex, 1, msg), StandardCharsets.UTF_8)
        val bottomText = URLEncoder.encode(RegexUtils.group(regex, 2, msg), StandardCharsets.UTF_8)
        MsgSendUtils.send(
            userId,
            groupId,
            bot,
            MsgUtils.builder().img("https://5000choyen.mikuac.com/image?top=${topText}&bottom=${bottomText}").build()
        )
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        buildMsg(event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        buildMsg(event.message, event.userId, event.groupId, bot)
        return super.onGroupMessage(bot, event)
    }

}