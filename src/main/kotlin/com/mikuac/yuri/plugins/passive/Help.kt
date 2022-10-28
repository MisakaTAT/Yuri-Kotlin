package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import org.springframework.stereotype.Component

@Shiro
@Component
class Help {

    @MessageHandler(cmd = RegexCMD.HELP)
    fun helpHandler(bot: Bot, event: AnyMessageEvent) {
        val msg = """
            使用教程: https://mikuac.com/archives/675
            GitHub: https://github.com/MisakaTAT/Yuri-Kotlin
            Powered By: https://github.com/MisakaTAT/Shiro
        """.trimIndent()
        bot.sendMsg(event, msg, false)
    }

}