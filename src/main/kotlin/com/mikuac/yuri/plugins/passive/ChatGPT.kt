package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.bean.dto.ChatGPTDto
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.NetUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class ChatGPT {

    private val headers = object : HashMap<String, String>() {
        init {
            put("Content-Type", "application/json; charset=utf-8")
            put("Authorization", "Bearer ${Config.plugins.chatGPT.token}")
        }
    }

    private fun request(prompt: String): String {
        val params = JsonObject()
        params.addProperty("model", Config.plugins.chatGPT.model)
        params.addProperty("prompt", prompt)
        params.addProperty("temperature", 0.9)
        params.addProperty("max_tokens", 4000)
        params.addProperty("top_p", 1)
        params.addProperty("frequency_penalty", 0.0)
        params.addProperty("presence_penalty", 0.6)
        val stop = JsonArray()
        stop.add(" Human:")
        stop.add(" AI:")
        params.add("stop", stop)
        val api = "https://api.openai.com/v1/completions"
        val data: ChatGPTDto
        try {
            val resp = NetUtils.post(api, headers, params.toString(), Config.plugins.chatGPT.proxy, 60)
            data = Gson().fromJson(resp.body?.string(), ChatGPTDto::class.java)
            resp.close()
        } catch (e: Exception) {
            e.printStackTrace()
            throw YuriException("ChatGPT 请求异常：${e.message}")
        }
        if (data.choices.isNotEmpty() && data.choices[0].text.isNotBlank()) {
            return data.choices[0].text
        } else {
            throw YuriException("好像出现了点小问题，要不再试试？")
        }
    }

    @MessageHandler(cmd = RegexCMD.CHAT_GPT)
    fun chatGPTHandler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        try {
            val msg = matcher.group(1)
            if (msg.isNullOrBlank()) throw YuriException("请输入正确的问题")
            bot.sendMsg(
                event,
                MsgUtils.builder().reply(event.messageId).text("处理中··· 等待时长不会超过1分钟").build(),
                false
            )
            bot.sendMsg(event, MsgUtils.builder().reply(event.messageId).text(request(msg).trim()).build(), false)
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}