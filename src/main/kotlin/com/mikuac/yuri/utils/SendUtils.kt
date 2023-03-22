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

    fun at(event: Any, bot: Bot, msg: String): Int? {
        if (event is AnyMessageEvent) {
            if (event.groupId != 0L) {
                return bot.sendGroupMsg(
                    event.groupId,
                    buildAtMsg(event.userId, msg),
                    false
                )?.data?.messageId
            }
            return bot.sendPrivateMsg(event.userId, msg, false)?.data?.messageId
        }
        if (event is GroupMessageEvent) return bot.sendGroupMsg(
            event.groupId,
            buildAtMsg(event.userId, msg),
            false
        )?.data?.messageId
        if (event is PrivateMessageEvent) return bot.sendPrivateMsg(event.userId, msg, false)?.data?.messageId
        return null
    }

    fun reply(msgId: Int, userId: Long, groupId: Long, bot: Bot, text: String): Int? {
        if (groupId != 0L) return bot.sendGroupMsg(
            groupId,
            MsgUtils.builder().reply(msgId).text(text).build(),
            false
        )?.data?.messageId
        return bot.sendPrivateMsg(userId, text, false)?.data?.messageId
    }

    fun reply(event: Any, bot: Bot, msg: String): Int? {
        if (event is AnyMessageEvent) return bot.sendMsg(
            event,
            buildReplyMsg(event.messageId, msg),
            false
        )?.data?.messageId
        if (event is GroupMessageEvent) return bot.sendGroupMsg(
            event.groupId,
            buildReplyMsg(event.messageId, msg),
            false
        )?.data?.messageId
        if (event is PrivateMessageEvent) return bot.sendPrivateMsg(
            event.userId,
            buildReplyMsg(event.messageId, msg),
            false
        )?.data?.messageId
        return null
    }

    private fun buildReplyMsg(msgId: Int, msg: String): String {
        return MsgUtils.builder().reply(msgId).text(msg).build()
    }

    private fun buildAtMsg(userId: Long, msg: String): String {
        return MsgUtils.builder().at(userId).text(msg).build()
    }

}