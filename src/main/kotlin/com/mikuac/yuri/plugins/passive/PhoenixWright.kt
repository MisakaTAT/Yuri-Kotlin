package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import org.springframework.stereotype.Component
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher

@Shiro
@Component
class PhoenixWright {

    private val cfg = Config.plugins.phoenixWright

    private fun buildMsg(matcher: Matcher): String {
        var topText = matcher.group("top") ?: throw YuriException("请输入顶部内容")
        var bottomText = matcher.group("bottom") ?: throw YuriException("请输入底部内容")
        topText = URLEncoder.encode(topText, StandardCharsets.UTF_8.toString())
        bottomText = URLEncoder.encode(bottomText, StandardCharsets.UTF_8.toString())
        val img = NetUtils.getBase64("https://gsapi.cbrx.io/image?top=${topText}&bottom=${bottomText}", cfg.proxy)
        return MsgUtils.builder().img(img).build()
    }

    @AnyMessageHandler(cmd = Regex.PHOENIX_WRIGHT)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            bot.sendMsg(event, buildMsg(matcher), false)
        }
    }

}