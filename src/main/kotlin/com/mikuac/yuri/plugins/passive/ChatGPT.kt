package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.ChatGPTDTO
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class ChatGPT {

    private val headers = object : HashMap<String, String>() {
        init {
            put("Content-Type", "application/json; charset=utf-8")
        }
    }

    private fun request(prompt: String): List<ChatGPTDTO.Result.Choice>? {
        val config = Config.plugins.chatGPT
        if (config.token.isBlank() || config.model.isBlank()) throw YuriException("未正确配置 OpenAI 令牌或模型")
        headers["Authorization"] = "Bearer ${config.token}"

        val params = JsonObject()
        params.addProperty("model", Config.plugins.chatGPT.model)
        params.addProperty("prompt", "$prompt。")
        params.addProperty("temperature", 0.9)
        params.addProperty("max_tokens", 4000)

        val data: ChatGPTDTO.Result
        val error: ChatGPTDTO.Error
        val api = "https://api.openai.com/v1/completions"
        val resp = NetUtils.post(api, headers, params.toString(), config.proxy, 60)
        if (resp.code == 400) {
            error = Gson().fromJson(resp.body?.string(), ChatGPTDTO.Error::class.java)
            resp.close()
            throw YuriException(error.error.message)
        }
        data = Gson().fromJson(resp.body?.string(), ChatGPTDTO.Result::class.java)
        return data.choices
    }

    @AnyMessageHandler(cmd = RegexCMD.CHAT_GPT)
    fun chatGPTHandler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        try {
            val prompt = matcher.group(1)
            if (prompt.isNullOrBlank()) return
            val resp = request(prompt) ?: throw YuriException("未知错误")
            if (resp.size > 1) {
                val contents = ArrayList<String>()
                resp.forEach {
                    contents.add(it.text.trim())
                }
                val msg = ShiroUtils.generateForwardMsg(Config.base.selfId, Config.base.nickname, contents)
                bot.sendForwardMsg(event, msg)
                return
            }
            SendUtils.reply(event, bot, resp[0].text.trim())
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}