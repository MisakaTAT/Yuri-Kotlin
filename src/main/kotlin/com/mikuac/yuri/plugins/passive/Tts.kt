package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class Tts {

    @AnyMessageHandler(cmd = Regex.TTS)
    fun handler(event: AnyMessageEvent, bot: Bot) {
        ExceptionHandler.with(bot, event) {
            val msg = event.arrayMsg.filter { it.type == MsgTypeEnum.text }.map { it.data["text"] }.joinToString()
            val text = RegexUtils.group(1, msg, Regex.TTS)
            if (text.trim().isBlank()) throw YuriException("非法输入")
            bot.sendMsg(event, MsgUtils.builder().tts(text.trim()).build(), false)
        }
    }

}