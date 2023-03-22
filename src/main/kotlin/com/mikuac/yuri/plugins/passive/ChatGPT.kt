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
import com.theokanning.openai.OpenAiHttpException
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

    private val proxy = Config.base.proxy

    private val cfg = Config.plugins.chatGPT

    private fun callChatCompletion(content: String): ChatCompletionResult? {
        if (cfg.token.isBlank() || cfg.model.isBlank()) throw YuriException("未正确配置 OpenAI 令牌或模型")
        var service = OpenAiService(cfg.token, Duration.ofSeconds(cfg.timeout.toLong()))
        if (cfg.proxy) {
            val mapper = defaultObjectMapper()
            val client = defaultClient(cfg.token, Duration.ofSeconds(cfg.timeout.toLong()))
                .newBuilder()
                .proxy(Proxy(Proxy.Type.valueOf(proxy.type), InetSocketAddress(proxy.host, proxy.port)))
                .build()
            val retrofit = defaultRetrofit(client, mapper)
            val api = retrofit.create(OpenAiApi::class.java)
            service = OpenAiService(api)
        }
        val messages = ArrayList<ChatMessage>()
        if (cfg.messages.isNotEmpty()) {
            cfg.messages.forEach {
                messages.add(ChatMessage(ChatMessageRole.valueOf(it.role.uppercase()).value(), it.content.trim()))
            }
        }
        messages.add(ChatMessage(ChatMessageRole.USER.value(), content.trim()))
        val chatCompletionRequest = ChatCompletionRequest.builder()
            .model(cfg.model)
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
        } catch (e: OpenAiHttpException) {
            if (e.message?.contains("Rate limit") == true) {
                SendUtils.reply(event, bot, "呜呜，人太多回答不过来了，稍后再来吧。")
                return
            }
            SendUtils.reply(event, bot, "ERROR: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

}