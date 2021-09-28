package com.mikuac.yuri.common.utils

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot

class MsgSendUtils {
    companion object {

        fun sendAll(userId: Long, groupId: Long, bot: Bot, text: String) {
            if (groupId != 0L) bot.sendGroupMsg(groupId, MsgUtils.builder().at(userId).text(text).build(), false)
            if (groupId == 0L) bot.sendPrivateMsg(userId, text, false)
        }

    }
}