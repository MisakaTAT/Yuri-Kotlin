package com.mikuac.yuri.utils

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot

object MsgSendUtils {

    fun atSend(userId: Long, groupId: Long, bot: Bot, text: String): Int {
        if (groupId != 0L) return bot.sendGroupMsg(
            groupId,
            MsgUtils.builder().at(userId).text(text).build(),
            false
        ).data.messageId
        return bot.sendPrivateMsg(userId, text, false).data.messageId
    }

    fun replySend(msgId: Int, userId: Long, groupId: Long, bot: Bot, text: String): Int {
        if (groupId != 0L) return bot.sendGroupMsg(
            groupId,
            MsgUtils.builder().reply(msgId).text(text).build(),
            false
        ).data.messageId
        return bot.sendPrivateMsg(userId, text, false).data.messageId
    }

}