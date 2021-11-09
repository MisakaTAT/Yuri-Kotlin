package com.mikuac.yuri.plugins.filter

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.common.utils.CheckUtils
import com.mikuac.yuri.common.utils.LogUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TencentBotFilter : BotPlugin() {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val userId = event.userId
        if (userId in 2854196300L..2854216399L) {
            if (checkUtils.pluginIsDisable(this.javaClass.simpleName)) return MESSAGE_IGNORE
            return MESSAGE_BLOCK
        }
        return MESSAGE_IGNORE
    }

}