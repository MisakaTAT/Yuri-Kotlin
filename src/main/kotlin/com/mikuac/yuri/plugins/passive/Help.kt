package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.enums.Regex
import org.springframework.stereotype.Component

@Shiro
@Component
class Help {

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.HELP)
    fun handler(bot: Bot, event: AnyMessageEvent) {
        val msg = """
            使用教程: https://mikuac.com/archives/675
            GitHub: https://github.com/MisakaTAT/Yuri-Kotlin
            Powered By: https://github.com/MisakaTAT/Shiro
        """.trimIndent()
        bot.sendMsg(event, msg, false)
    }

}