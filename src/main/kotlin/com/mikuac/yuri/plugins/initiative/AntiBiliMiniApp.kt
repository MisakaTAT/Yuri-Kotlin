package com.mikuac.yuri.plugins.initiative

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.enums.MsgTypeEnum
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.RegexUtils
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class AntiBiliMiniApp {

    data class AntiBiliMiniApp(
        val code: Int,
        val data: Data,
        val message: String,
    ) {
        data class Data(
            val bvid: String,
            val pic: String,
            val title: String,
            val owner: Owner,
            val stat: Stat
        ) {
            data class Owner(
                val name: String,
            )

            data class Stat(
                val aid: Int,
                val view: Int,
                val danmaku: Int,
                val reply: Int,
                val coin: Int,
                val share: Int,
                val like: Int
            )
        }
    }

    private fun request(shortURL: String): AntiBiliMiniApp {
        val data: AntiBiliMiniApp
        val urlResp = NetUtils.get(shortURL)
        val bid = RegexUtils.group(Regex("(?<=video/)(.*)(?=/\\?)"), 1, urlResp.request.url.toString())
        urlResp.close()
        val api = "https://api.bilibili.com/x/web-interface/view?bvid=${bid}"
        val resp = NetUtils.get(api)
        data = Gson().fromJson(resp.body?.string(), AntiBiliMiniApp::class.java)
        resp.close()
        if (data.code != 0) throw YuriException(data.message)
        return data
    }

    private fun buildMsg(json: String): String {
        val jsonObject = JsonParser.parseString(json)
        val shortURL = jsonObject.asJsonObject["meta"].asJsonObject["detail_1"].asJsonObject["qqdocurl"].asString
        val data = request(shortURL).data
        return MsgUtils.builder()
            .img(data.pic)
            .text("\n${ShiroUtils.escape2(data.title)}")
            .text("\nUP：${ShiroUtils.escape2(data.owner.name)}")
            .text("\n播放：${data.stat.view} 弹幕：${data.stat.danmaku}")
            .text("\n投币：${data.stat.coin} 点赞：${data.stat.like}")
            .text("\n评论：${data.stat.reply} 分享：${data.stat.share}")
            .text("\nhttps://www.bilibili.com/video/av${data.stat.aid}")
            .text("\nhttps://www.bilibili.com/video/${data.bvid}")
            .build()
    }

    @AnyMessageHandler
    fun handler(bot: Bot, event: AnyMessageEvent) {
        try {
            val msg = event.message
            if (!msg.contains("com.tencent.miniapp_01") || !msg.contains("哔哩哔哩")) return
            val json = event.arrayMsg.filter { it.type == MsgTypeEnum.json }
            if (json.isNotEmpty()) {
                bot.sendMsg(event, json[0].data["data"]?.let { buildMsg(it) }, false)
            }
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}
