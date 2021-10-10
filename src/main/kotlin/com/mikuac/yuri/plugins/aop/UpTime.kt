package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import org.springframework.stereotype.Component


@Component
class UpTime : BotPlugin() {

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        // TODO:
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        // TODO:
        return MESSAGE_IGNORE
    }

}