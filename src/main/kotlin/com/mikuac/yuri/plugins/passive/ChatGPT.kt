package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.SendUtils
import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import com.theokanning.openai.service.OpenAiService.*
import org.springframework.stereotype.Component
import java.net.InetSocketAddress
import java.net.Proxy
import java.time.Duration
import java.util.regex.Matcher

@Shiro
@Component
class ChatGPT {

    private fun callChatCompletion(content: String): ChatCompletionResult? {
        val proxy = Config.base.proxy
        val config = Config.plugins.chatGPT
        if (config.token.isBlank() || config.model.isBlank()) throw YuriException("未正确配置 OpenAI 令牌或模型")
        var service = OpenAiService(config.token)
        if (config.proxy) {
            val mapper = defaultObjectMapper()
            val client = defaultClient(config.token, Duration.ofSeconds(10))
                .newBuilder()
                .proxy(Proxy(Proxy.Type.valueOf(proxy.type), InetSocketAddress(proxy.host, proxy.port)))
                .build()
            val retrofit = defaultRetrofit(client, mapper)
            val api = retrofit.create(OpenAiApi::class.java)
            service = OpenAiService(api)
        }
        val messages = ArrayList<ChatMessage>()
        if (config.messages.isNotEmpty()) {
            config.messages.forEach {
                messages.add(ChatMessage(ChatMessageRole.valueOf(it.role).value(), it.content.trim()))
            }
        }
        messages.add(ChatMessage(ChatMessageRole.USER.value(), content.trim()))
        val chatCompletionRequest = ChatCompletionRequest.builder()
            .model(config.model)
            .messages(messages)
            .build()
        return service.createChatCompletion(chatCompletionRequest)
    }

    @AnyMessageHandler(cmd = RegexCMD.CHAT_GPT)
    fun chatGPTHandler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        try {
            val content = matcher.group(1)
            if (content.isNullOrBlank()) return
            val choices = callChatCompletion(content)?.choices ?: throw YuriException("ChatGPT 返回为空")
            if (choices.isNotEmpty()) {
                SendUtils.reply(event, bot, choices[0].message.content.trim())
            }
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}