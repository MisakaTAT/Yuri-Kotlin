package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.CheckUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HttpCat : BotPlugin() {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private fun check(msg: String, msgId: Int, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.HTTP_CAT.value)) return
        // if (!checkUtils.basicCheck(this.javaClass.simpleName, userId, groupId, bot)) return
        buildMsg(msg, msgId, userId, groupId, bot)
    }

    private fun buildMsg(msg: String, msgId: Int, userId: Long, groupId: Long, bot: Bot) {
        val statusCode = RegexUtils.group(RegexEnum.HTTP_CAT.value, 1, msg)
        if (statusCode.isNotEmpty()) {
            val picMsg = MsgUtils.builder().img("https://http.cat/${statusCode}").build()
            MsgSendUtils.replySend(msgId, userId, groupId, bot, picMsg)
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        check(event.message, event.messageId, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(event.message, event.messageId, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}