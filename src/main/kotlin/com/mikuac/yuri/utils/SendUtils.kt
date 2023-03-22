package com.mikuac.yuri.utils

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent

object SendUtils {

    fun at(userId: Long, groupId: Long, bot: Bot, text: String): Int? {
        if (groupId != 0L) return bot.sendGroupMsg(
            groupId,
            MsgUtils.builder().at(userId).text(text).build(),
            false
        )?.data?.messageId

        return bot.sendPrivateMsg(userId, text, false)?.data?.messageId
    }

    fun reply(event: Any, bot: Bot, msg: String): Int? {
        when (event) {
            is AnyMessageEvent -> {
                event.let {
                    return bot.sendMsg(it, buildReply(it.messageId, msg), false)?.data?.messageId
                }
            }

            is GroupMessageEvent -> {
                event.let {
                    return bot.sendGroupMsg(it.groupId, buildReply(it.messageId, msg), false)?.data?.messageId
                }
            }

            is PrivateMessageEvent -> {
                event.let {
                    return bot.sendPrivateMsg(it.userId, buildReply(it.messageId, msg), false)?.data?.messageId
                }
            }

            else -> return null
        }
    }

    private fun buildReply(msgId: Int, msg: String): String {
        return MsgUtils.builder().reply(msgId).text(msg).build()
    }

}