package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class EventLog : BotPlugin() {

    private val log = KotlinLogging.logger {}

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        log.info { "Received group msg - Group: ${event.groupId} User: ${event.userId} Msg: ${event.message}" }
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        log.info { "Received private msg - User: ${event.userId} Msg: ${event.message}" }
        return MESSAGE_IGNORE
    }

}