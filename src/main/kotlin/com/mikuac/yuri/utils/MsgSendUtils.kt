package com.mikuac.yuri.utils

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot

class MsgSendUtils {
    companion object {

        fun send(userId: Long, groupId: Long, bot: Bot, text: String): Int {
            if (groupId != 0L) return bot.sendGroupMsg(
                groupId,
                MsgUtils.builder().text(text).build(),
                false
            ).data.messageId
            if (groupId == 0L) return bot.sendPrivateMsg(userId, text, false).data.messageId
            return 0
        }

        fun atSend(userId: Long, groupId: Long, bot: Bot, text: String): Int {
            if (groupId != 0L) return bot.sendGroupMsg(
                groupId,
                MsgUtils.builder().at(userId).text(text).build(),
                false
            ).data.messageId
            if (groupId == 0L) return bot.sendPrivateMsg(userId, text, false).data.messageId
            return 0
        }

        fun replySend(msgId: Int, userId: Long, groupId: Long, bot: Bot, text: String): Int {
            if (groupId != 0L) return bot.sendGroupMsg(
                groupId,
                MsgUtils.builder().reply(msgId).text(text).build(),
                false
            ).data.messageId
            if (groupId == 0L) return bot.sendPrivateMsg(userId, text, false).data.messageId
            return 0
        }

        fun errorSend(userId: Long, groupId: Long, bot: Bot, tips: String, error: String?): Int {
            if (groupId != 0L) return bot.sendGroupMsg(
                groupId,
                MsgUtils.builder().at(userId)
                    .text(tips)
                    .text(if (error != null) "\n[详情] $error" else "")
                    .build(),
                false
            ).data.messageId
            if (groupId == 0L) return bot.sendPrivateMsg(
                userId,
                tips + if (error != null) "\n[详情] $error" else "",
                false
            ).data.messageId
            return 0
        }

    }
}