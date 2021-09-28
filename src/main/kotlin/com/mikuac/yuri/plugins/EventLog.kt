package com.mikuac.yuri.plugins

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.log.Slf4j.Companion.log
import org.springframework.stereotype.Component

@Component
class EventLog : BotPlugin() {

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        log.info("群组消息：群号[${event.groupId}] 用户[${event.userId}] 消息[${event.message}]")
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        log.info("私聊消息：群号[${event.userId}] 消息[${event.message}]")
        return MESSAGE_IGNORE
    }

}