package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.dto.NsfwDTO
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.NetUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class Nsfw {

    private fun request(img: String): NsfwDTO {
        val data: NsfwDTO
        try {
            val api = "https://nsfwtag.azurewebsites.net/api/nsfw?url=${img}"
            val resp = NetUtils.get(api)
            data = Gson().fromJson(resp.body?.string(), NsfwDTO::class.java)
            resp.close()
        } catch (e: Exception) {
            throw YuriException("NSFW鉴定失败：${e.message}")
        }
        return data
    }

    private fun judge(p: NsfwDTO.Item): String {
        if (p.neutral > 0.3) {
            return "就这？"
        }
        var c: String = if (p.drawings > 0.3 || p.neutral < 0.3) {
            "二次元"
        } else {
            "三次元"
        }
        if (p.hentai > 0.3) {
            c += " hentai"
        }
        if (p.porn > 0.3) {
            c += " porn"
        }
        if (p.sexy > 0.3) {
            c += " hso"
        }
        return c
    }

    @MessageHandler(cmd = RegexCMD.NSFW)
    fun nsfwHandler(event: AnyMessageEvent, bot: Bot) {
        try {
            val images = event.arrayMsg.filter { it.type == "image" }
            if (images.isEmpty()) throw YuriException("没有发现需要鉴定的图片")
            if (images.size > 1) throw YuriException("一次只能处理一张图片哦～")
            val data = images[0].data["url"]?.let { request(it) }
            if (data != null && data.isEmpty()) throw YuriException("鉴定失败，可能是网络炸惹 QAQ")
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, judge(data!![0]))
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}