package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.entity.ChatGPTEntity
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.ChatGPTRepository
import com.mikuac.yuri.utils.SendUtils
import com.theokanning.openai.OpenAiApi
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.completion.chat.ChatMessageRole
import com.theokanning.openai.service.OpenAiService
import com.theokanning.openai.service.OpenAiService.*
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    private lateinit var repository: ChatGPTRepository

    private fun callChatCompletion(userId: Long, prompt: String): ChatCompletionResult? {
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
        val local = repository.findByUserId(userId)
        val messages = ArrayList<ChatMessage>()
        if (local.isPresent) {
            messages.add(ChatMessage(ChatMessageRole.SYSTEM.value(), local.get().personality))
        } else {
            if (cfg.messages.isNotEmpty()) {
                cfg.messages.forEach {
                    messages.add(ChatMessage(ChatMessageRole.valueOf(it.role.uppercase()).value(), it.content.trim()))
                }
            }
        }
        println(messages)
        messages.add(ChatMessage(ChatMessageRole.USER.value(), prompt.trim()))
        val chatCompletionRequest = ChatCompletionRequest.builder()
            .model(cfg.model)
            .messages(messages)
            .build()
        return service.createChatCompletion(chatCompletionRequest)
    }

    @Suppress("kotlin:S3776")
    @AnyMessageHandler(cmd = RegexCMD.CHAT_GPT)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            val userId = event.userId
            var prompt = matcher.group("prompt")

            when (matcher.group("action")) {
                "set" -> {
                    if (prompt.isNullOrBlank()) throw YuriException("请提供有效的预设文本")
                    prompt = prompt.trim()
                    if (prompt.isBlank()) throw YuriException("请提供有效的上下文")
                    val local = repository.findByUserId(userId)
                    if (local.isPresent) {
                        local.get().personality = prompt
                        repository.save(local.get())
                    } else {
                        repository.save(ChatGPTEntity(0, prompt.trim(), userId))
                    }
                    SendUtils.reply(event, bot, "预设保存成功")
                    return@with
                }

                "del" -> {
                    repository.deleteByUserId(userId)
                    SendUtils.reply(event, bot, "预设删除成功")
                    return@with
                }

                "show" -> {
                    val local = repository.findByUserId(userId)
                    if (local.isPresent) {
                        SendUtils.reply(event, bot, local.get().personality)
                    } else {
                        SendUtils.reply(event, bot, "暂无预设")
                    }
                    return@with
                }
            }

            prompt = prompt.trim()
            if (prompt.isBlank()) throw YuriException("请提供有效的上下文")
            val choices = callChatCompletion(userId, prompt)?.choices ?: throw YuriException("OpenAI 返回了空数据")
            if (choices.isNotEmpty()) {
                SendUtils.reply(event, bot, choices[0].message.content.trim())
            }
        }
    }

}