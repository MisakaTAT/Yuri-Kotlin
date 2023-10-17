package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class HttpCat {

    private fun buildMsg(matcher: Matcher): String? {
        val statusCode = matcher.group(1) ?: throw YuriException("状态码提取失败，请检查是否输入正确。")
        return MsgUtils.builder().img("https://http.cat/${statusCode}").build()
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.HTTP_CAT)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            val msg = buildMsg(matcher)
            bot.sendMsg(event, msg, false)
        }
    }

}