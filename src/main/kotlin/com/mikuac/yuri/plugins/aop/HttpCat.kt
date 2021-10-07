package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.RegexUtils
import org.springframework.stereotype.Component

@Component
class HttpCat : BotPlugin() {

    private val regex = Regex("(?i)httpcat\\s([0-9]+)")

    private val api = ReadConfig.config.plugin.httpCat.api

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            val statusCode = RegexUtils.group(regex, 1, msg)
            if (statusCode.isNotEmpty()) {
                val msgUtils = MsgUtils.builder().img(api + statusCode).build()
                bot.sendPrivateMsg(event.userId, msgUtils, false)
            }
        }
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            val statusCode = RegexUtils.group(regex, 1, msg)
            if (statusCode.isNotEmpty()) {
                val msgUtils = MsgUtils.builder().reply(event.messageId).img(api + statusCode).build()
                bot.sendGroupMsg(event.groupId, msgUtils, false)
            }
        }
        return MESSAGE_IGNORE
    }

}