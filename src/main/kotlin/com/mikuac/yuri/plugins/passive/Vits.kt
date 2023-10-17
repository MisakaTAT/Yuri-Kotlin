@file:Suppress("SpellCheckingInspection")

package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.VitsDTO
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class Vits {

    private val cfg = Config.plugins.vits

    private val speakersId = ArrayList<Int>()

    private val speakers: VitsDTO.Speakers by lazy {
        NetUtils.get("${cfg.api}/voice/speakers").use { resp ->
            resp.code.let { if (it != 200) throw YuriException("服务器出现错误：${it}") }
            val speakers = Gson().fromJson(resp.body?.string(), VitsDTO.Speakers::class.java)
            speakers.vits.forEach {
                speakersId.add(it.keys.toList()[0].toInt())
            }
            speakers
        }
    }

    private fun voice(text: String, model: Int, bot: Bot, event: AnyMessageEvent) {
        if (speakersId.isEmpty()) speakers
        if (text.isBlank()) throw YuriException("需要转换的文本内容为空")
        if (!speakersId.contains(model)) throw YuriException("当前 ID 不存在，请使用 vits models 查询可用模型")
        // https://github.com/Artrajz/vits-simple-api 的 silk 似乎有问题
        // 所以请求 wav 格式然后给 go-cqhttp 装上 ffmpeg 让 go-cqhttp 做转换 :)
        val url = "${cfg.api}/voice?lang=auto&format=wav&id=${model}&text=${text}"
        bot.sendMsg(event, MsgUtils.builder().voice(NetUtils.getBase64(url, false)).build(), false)
    }

    private fun models(bot: Bot, event: AnyMessageEvent) {
        MsgUtils.builder().let { builder ->
            speakers.vits.forEachIndexed { index, it ->
                builder.text("${if (index == 0) "" else "\n"}${it.keys.toList()[0]} | ${it.values.toList()[0]}")
            }
            builder.build()
        }.let {
            bot.sendMsg(event, it, false)
        }
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.VITS)
    fun handler(event: AnyMessageEvent, bot: Bot, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            if (cfg.api.isBlank()) throw YuriException("请配置 VITS 接口")
            val model = matcher.group("model")?.trim()?.toInt() ?: 0
            when (val text = matcher.group("text")?.trim() ?: "") {
                "models" -> models(bot, event)
                else -> voice(text, model, bot, event)
            }
        }
    }

}