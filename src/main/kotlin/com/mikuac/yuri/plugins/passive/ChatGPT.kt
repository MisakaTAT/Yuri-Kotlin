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
import com.theokanning.openai.OpenAiHttpException
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
import java.net.SocketTimeoutException
import java.time.Duration
import java.util.regex.Matcher
import java.util.regex.Pattern

@Shiro
@Component
class ChatGPT {

    private val proxy = Config.base.proxy

    private val cfg = Config.plugins.chatGPT

    @Autowired
    private lateinit var repository: ChatGPTRepository

    val lock = ArrayList<Long>()

    private fun call(userId: Long, prompt: String): ChatCompletionResult? {
        if (lock.contains(userId)) throw YuriException("您的上一条问题还在处理中，请耐心等待···")
        if (cfg.token.isBlank() || cfg.model.isBlank()) throw YuriException("未正确配置 OpenAI 令牌或模型")

        val service = createOpenAiService()
        val messages = setMessages(userId)
        messages.add(ChatMessage(ChatMessageRole.USER.value(), prompt.trim()))

        val chatCompletionRequest = ChatCompletionRequest.builder()
            .model(cfg.model)
            .messages(messages)
            .build()

        try {
            lock.add(userId)
            return service.createChatCompletion(chatCompletionRequest)
        } catch (e: OpenAiHttpException) {
            e.message?.let {
                if (it.contains("Rate limit")) throw YuriException(handleRateLimitError(it))
            }
        } catch (e: RuntimeException) {
            if (e.cause is SocketTimeoutException) {
                throw YuriException("回答超时，请适当减少生成的文本量。")
            }
            throw e
        } finally {
            lock.remove(userId)
        }
        return null
    }

    private fun createOpenAiService(): OpenAiService {
        val clientBuilder = defaultClient(cfg.token, Duration.ofSeconds(cfg.timeout.toLong())).newBuilder()
        if (cfg.proxy) {
            clientBuilder.proxy(Proxy(Proxy.Type.valueOf(proxy.type), InetSocketAddress(proxy.host, proxy.port)))
        }
        val client = clientBuilder.build()
        val mapper = defaultObjectMapper()
        val retrofit = defaultRetrofit(client, mapper)
        val api = retrofit.create(OpenAiApi::class.java)
        return OpenAiService(api)
    }

    private fun setMessages(userId: Long): ArrayList<ChatMessage> {
        val messages = ArrayList<ChatMessage>()
        val local = repository.findByUserId(userId)
        when {
            local.isPresent -> messages.add(ChatMessage(ChatMessageRole.SYSTEM.value(), local.get().preset))
            else -> cfg.messages.forEach {
                messages.add(ChatMessage(ChatMessageRole.valueOf(it.role.uppercase()).value(), it.content.trim()))
            }
        }
        return messages
    }

    private fun setPreset(userId: Long, prompt: String, bot: Bot, event: AnyMessageEvent) {
        if (prompt.isBlank()) throw YuriException("请提供有效的预设文本")
        val local = repository.findByUserId(userId).orElseGet { ChatGPTEntity(0, prompt, userId) }
        local.preset = prompt
        repository.save(local)
        SendUtils.reply(event, bot, "预设保存成功")
    }

    private fun deletePreset(userId: Long, bot: Bot, event: AnyMessageEvent) {
        repository.deleteByUserId(userId)
        SendUtils.reply(event, bot, "预设删除成功")
    }

    private fun showPreset(userId: Long, bot: Bot, event: AnyMessageEvent) {
        val local = repository.findByUserId(userId)
        val message = local.map { it.preset }.orElse("暂无预设")
        SendUtils.reply(event, bot, message)
    }

    private fun processPrompt(userId: Long, prompt: String, bot: Bot, event: AnyMessageEvent) {
        if (prompt.isBlank()) throw YuriException("请提供有效的上下文")
        val choices = call(userId, prompt)?.choices ?: throw YuriException("OpenAI 返回了空数据")
        if (choices.isNotEmpty()) {
            SendUtils.reply(event, bot, choices[0].message.content.trim())
        }
    }

    fun handleRateLimitError(errMsg: String): String {
        val regex = Pattern.compile("""Limit: (\d+) / min\. Current: (\d+) / min""")
        val matcher = regex.matcher(errMsg)
        if (matcher.find()) {
            val limit = matcher.group(1).toInt()
            val current = matcher.group(2).toInt()
            return "API调用过快，限制$limit/分钟，当前$current/分钟`。"
        }
        return errMsg
    }

    @AnyMessageHandler(cmd = RegexCMD.CHAT_GPT)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            val userId = event.userId
            val prompt = matcher.group("prompt")?.trim() ?: ""
            when (matcher.group("action")?.trim() ?: "") {
                "set" -> setPreset(userId, prompt, bot, event)
                "del" -> deletePreset(userId, bot, event)
                "show" -> showPreset(userId, bot, event)
                else -> processPrompt(userId, prompt, bot, event)
            }
        }
    }

}