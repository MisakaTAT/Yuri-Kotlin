package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class PhoenixWright : BotPlugin() {

    private fun buildMsg(msg: String): String {
        val regex = RegexCMD.PHOENIX_WRIGHT.toRegex()
        val topText = URLEncoder.encode(RegexUtils.group(regex, 1, msg), StandardCharsets.UTF_8)
        val bottomText = URLEncoder.encode(RegexUtils.group(regex, 2, msg), StandardCharsets.UTF_8)
        return MsgUtils.builder().img("https://5000choyen.mikuac.com/image?top=${topText}&bottom=${bottomText}").build()
    }

    @MessageHandler(cmd = RegexCMD.PHOENIX_WRIGHT)
    fun phoenixWrightHandler(bot: Bot, event: WholeMessageEvent) {
        try {
            val msg = buildMsg(event.message)
            bot.sendMsg(event, msg, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}