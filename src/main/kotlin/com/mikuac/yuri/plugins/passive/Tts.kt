package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class Tts {

    @MessageHandler(cmd = RegexCMD.TTS)
    fun ttsHandler(event: WholeMessageEvent, bot: Bot, matcher: Matcher) {
        val txt = matcher.group(1)
        if (txt != null && txt.isNotBlank()) bot.sendMsg(event, MsgUtils.builder().tts(txt.trim()).build(), false)
    }

}