package com.mikuac.yuri.plugins.filter

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.common.utils.LogUtils
import org.springframework.stereotype.Component

@Component
class TencentBotFilter : BotPlugin() {

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val userId = event.userId
        val groupId = event.groupId
        if (userId in 2854196300L..2854216399L) {
            LogUtils.action(userId, groupId, this.javaClass.simpleName, "")
            return MESSAGE_BLOCK
        }
        return MESSAGE_IGNORE
    }

}