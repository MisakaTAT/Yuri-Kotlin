package com.mikuac.yuri.plugins.filter

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.common.utils.LogUtils
import org.springframework.stereotype.Component

@Component
class TencentBotFilter : BotPlugin() {

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        if (event.userId in 2854196300L..2854216399L) {
            LogUtils.debug("过滤腾讯官方BOT消息（账号：${event.userId} 群：${event.groupId} 消息：${event.message}）")
            return MESSAGE_BLOCK
        }
        return MESSAGE_IGNORE
    }

}