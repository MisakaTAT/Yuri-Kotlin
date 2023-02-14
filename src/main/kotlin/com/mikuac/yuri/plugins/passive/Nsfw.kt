package com.mikuac.yuri.plugins.passive

import com.alibaba.fastjson2.to
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class Nsfw {

    class Nsfw : ArrayList<Nsfw.Item>() {
        data class Item(
            val drawings: Double,
            val hentai: Double,
            val neutral: Double,
            val porn: Double,
            val sexy: Double
        )
    }

    private fun request(img: String): Nsfw {
        val data: Nsfw
        val api = "https://nsfwtag.azurewebsites.net/api/nsfw?url=${img}"
        val resp = NetUtils.get(api)
        data = resp.body?.string().to<Nsfw>()
        resp.close()
        return data
    }

    private fun judge(p: Nsfw.Item): String {
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

    @AnyMessageHandler(cmd = RegexCMD.NSFW)
    fun nsfwHandler(event: AnyMessageEvent, bot: Bot) {
        try {
            val images = event.arrayMsg.filter { it.type == MsgTypeEnum.image }
            if (images.isEmpty()) throw YuriException("没有发现需要鉴定的图片")
            if (images.size > 1) throw YuriException("一次只能处理一张图片哦～")
            val data = images[0].data["url"]?.let { request(it) }
            if (data != null && data.isEmpty()) throw YuriException("鉴定失败，可能是网络炸惹 QAQ")
            SendUtils.reply(event.messageId, event.userId, event.groupId, bot, judge(data!![0]))
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}