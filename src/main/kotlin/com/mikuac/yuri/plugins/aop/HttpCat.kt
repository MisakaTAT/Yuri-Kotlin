package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.CheckUtils
import com.mikuac.yuri.common.utils.LogUtils
import com.mikuac.yuri.common.utils.MsgSendUtils
import com.mikuac.yuri.common.utils.RegexUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HttpCat : BotPlugin() {

    private val regex = Regex("(?i)httpcat\\s([0-9]+)")

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private fun buildMsg(msg: String, msgId: Int, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(regex)) return
        if (!checkUtils.basicCheck(this.javaClass.simpleName, userId, groupId, bot)) return
        val statusCode = RegexUtils.group(regex, 1, msg)
        if (statusCode.isNotEmpty()) {
            val picMsg = MsgUtils.builder().img("https://http.cat/${statusCode}").build()
            MsgSendUtils.replySend(msgId, userId, groupId, bot, picMsg)
        }
        LogUtils.action(userId, groupId, this.javaClass.simpleName, "Http Status Code: $statusCode")
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        buildMsg(event.message, event.messageId, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        buildMsg(event.message, event.messageId, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}