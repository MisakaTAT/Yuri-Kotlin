package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.MsgSendUtils
import org.springframework.stereotype.Component

@Component
class Help : BotPlugin() {

    private fun buildMsg(msgId: Int, msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.HELP.value)) return
        MsgSendUtils.replySend(
            msgId, userId, groupId, bot, """
            该项目为 YuriBot 的 Kotlin 重构版。
            当前正在缓慢重构中，暂无帮助文档。
            GitHub: https://github.com/MisakaTAT/Yuri-Kotlin
            Powered By: https://github.com/MisakaTAT/Shiro
        """.trimIndent()
        )
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        buildMsg(event.messageId, event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        buildMsg(event.messageId, event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}