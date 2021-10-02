package com.mikuac.yuri.plugins.filter

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.common.log.Slf4j.Companion.log
import org.springframework.stereotype.Component

@Component
class TencentBotFilter : BotPlugin() {

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val userId = event.userId
        val startId = 2854196300L
        val endId = 2854216399L
        if (userId in startId..endId) {
            log.info("已拦截QQ官方机器人消息 内容：${event.message}")
            return MESSAGE_BLOCK
        }
        return MESSAGE_IGNORE
    }

}