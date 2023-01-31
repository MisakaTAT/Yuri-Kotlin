package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.RegexUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class Tts {

    @AnyMessageHandler(cmd = RegexCMD.TTS)
    fun ttsHandler(event: AnyMessageEvent, bot: Bot) {
        try {
            val msg = event.arrayMsg.filter { it.type == MsgTypeEnum.text }.map { it.data["text"] }.joinToString()
            val regex = RegexUtils.regexMatcher(RegexCMD.TTS, msg) ?: throw YuriException("非法输入")
            val txt = regex.group(1).trim()
            if (txt.isBlank()) throw YuriException("非法输入")
            bot.sendMsg(event, MsgUtils.builder().tts(txt).build(), false)
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}