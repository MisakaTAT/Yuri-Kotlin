package com.mikuac.yuri.plugins.filter

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
class TencentBotFilter : BotPlugin() {

    private val log = KotlinLogging.logger {}

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        if (event.userId in 2854196300L..2854216399L) {
            log.info { "Filter tencent bot msg - Account: ${event.userId} Group: ${event.groupId}" }
            return MESSAGE_BLOCK
        }
        return MESSAGE_IGNORE
    }

}