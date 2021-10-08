package com.mikuac.yuri.plugins

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.LogUtils
import org.springframework.stereotype.Component

@Component
class EventLog : BotPlugin() {

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        LogUtils.debug("收到群组消息（群号：${event.groupId} 用户：${event.userId} 消息：${event.message}）")
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        LogUtils.debug("收到私聊消息（用户：${event.userId} 消息：${event.message}）")
        return MESSAGE_IGNORE
    }

}