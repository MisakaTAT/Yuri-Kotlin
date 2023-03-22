package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.dto.HitokotoDTO
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.utils.NetUtils
import org.springframework.stereotype.Component
import java.util.*

@Shiro
@Component
class Hitokoto {

    private val typesMap = object : HashMap<String, String>() {
        init {
            put("a", "动画")
            put("b", "漫画")
            put("c", "游戏")
            put("d", "文学")
            put("e", "原创")
            put("f", "来自网络")
            put("g", "其他")
            put("h", "影视")
            put("i", "诗词")
            put("j", "网易云")
            put("k", "哲学")
            put("l", "抖机灵")
        }
    }

    val types = arrayOf("a", "b", "c", "d", "h", "i", "j")

    private fun request(): HitokotoDTO {
        val data: HitokotoDTO
        val type = types[Random().nextInt(types.size)]
        val api = "https://v1.hitokoto.cn?c=${type}"
        val resp = NetUtils.get(api)
        data = Gson().fromJson(resp.body?.string(), HitokotoDTO::class.java)
        resp.close()
        return data
    }

    @AnyMessageHandler(cmd = RegexCMD.HITOKOTO)
    fun handler(bot: Bot, event: AnyMessageEvent) {
        ExceptionHandler.with(bot, event) {
            val data = request()
            val msg = MsgUtils.builder()
                .reply(event.messageId)
                .text("『${data.hitokoto}』")
                .text("\n出自：${data.from}")
                .text("\n类型：${typesMap[data.type]}")
                .build()
            bot.sendMsg(event, msg, false)
        }
    }

}