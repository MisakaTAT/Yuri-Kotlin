package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher

@Component
class PhoenixWright : BotPlugin() {

    private fun buildMsg(matcher: Matcher): String {
        var topText = matcher.group(1) ?: throw YuriException("请输入顶部内容")
        var bottomText = matcher.group(2) ?: throw YuriException("请输入底部内容")
        topText = URLEncoder.encode(topText, StandardCharsets.UTF_8)
        bottomText = URLEncoder.encode(bottomText, StandardCharsets.UTF_8)
        return MsgUtils.builder().img("https://5000choyen.mikuac.com/image?top=${topText}&bottom=${bottomText}").build()
    }

    @MessageHandler(cmd = RegexCMD.PHOENIX_WRIGHT)
    fun phoenixWrightHandler(bot: Bot, event: WholeMessageEvent, matcher: Matcher) {
        try {
            val msg = buildMsg(matcher)
            bot.sendMsg(event, msg, false)
        } catch (e: YuriException) {
            bot.sendMsg(event, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}