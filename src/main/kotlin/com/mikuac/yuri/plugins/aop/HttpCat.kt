package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.MsgSendUtils
import com.mikuac.yuri.common.utils.RegexUtils
import org.springframework.stereotype.Component

@Component
class HttpCat : BotPlugin() {

    private val regex = Regex("(?i)httpcat\\s([0-9]+)")

    private fun buildMsg(msgId: Int, userId: Long, groupId: Long, bot: Bot, msg: String) {
        val api = ReadConfig.config.plugin.httpCat.api
        val statusCode = RegexUtils.group(regex, 1, msg)
        if (statusCode.isNotEmpty()) {
            val picMsg = MsgUtils.builder().img(api + statusCode).build()
            MsgSendUtils.sendAll(msgId, userId, groupId, bot, picMsg)
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            buildMsg(event.messageId, event.userId, 0L, bot, msg)
        }
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            buildMsg(event.messageId, event.userId, event.groupId, bot, msg)
        }
        return MESSAGE_IGNORE
    }

}