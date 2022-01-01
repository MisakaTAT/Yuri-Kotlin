package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component

@Component
class HttpCat : BotPlugin() {

    private fun buildMsg(msg: String): String? {
        val statusCode = RegexUtils.group(RegexCMD.HTTP_CAT.toRegex(), 1, msg)
        if (statusCode.isNotEmpty()) return MsgUtils.builder().img("https://http.cat/${statusCode}").build()
        return "状态码提取失败，请检查是否输入正确。"
    }

    @MessageHandler(cmd = RegexCMD.HTTP_CAT)
    fun httpCatHandler(bot: Bot, event: WholeMessageEvent) {
        try {
            val msg = buildMsg(event.message)
            bot.sendMsg(event, msg, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}