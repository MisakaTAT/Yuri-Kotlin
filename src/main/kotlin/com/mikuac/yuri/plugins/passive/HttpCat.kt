package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class HttpCat {

    private fun buildMsg(matcher: Matcher): String? {
        val statusCode = matcher.group(1) ?: throw YuriException("状态码提取失败，请检查是否输入正确。")
        return MsgUtils.builder().img("https://http.cat/${statusCode}").build()
    }

    @AnyMessageHandler(cmd = RegexCMD.HTTP_CAT)
    fun httpCatHandler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        try {
            val msg = buildMsg(matcher)
            bot.sendMsg(event, msg, false)
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}