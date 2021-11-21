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

@Component
class HttpCat : BotPlugin() {

    private fun buildMsg(msg: String, msgId: Int, userId: Long, groupId: Long, bot: Bot) {
        val statusCode = RegexUtils.group(RegexEnum.HTTP_CAT.value, 1, msg)
        if (statusCode.isNotEmpty()) {
            val picMsg = MsgUtils.builder().img("https://http.cat/${statusCode}").build()
            MsgSendUtils.replySend(msgId, userId, groupId, bot, picMsg)
        }
    }

    private fun handler(msg: String, msgId: Int, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.HTTP_CAT.value)) return
        buildMsg(msg, msgId, userId, groupId, bot)
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        handler(event.message, event.messageId, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        handler(event.message, event.messageId, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}