package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.AnimePicDTO
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.SendUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Slf4j
@Shiro
@Component
class AnimePic {

    private val expiringMap: ExpiringMap<Long, Long> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(Config.plugins.animePic.cd.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    private fun request(r18: Boolean): AnimePicDTO.Data {
        val data: AnimePicDTO
        var api = "https://api.lolicon.app/setu/v2"
        if (r18) api = "$api?r18=1"
        val resp = NetUtils.get(api)
        data = Gson().fromJson(resp.body?.string(), AnimePicDTO::class.java)
        resp.close()
        if (data.error.isNotEmpty()) throw YuriException(data.error)
        if (data.data.isEmpty()) throw YuriException("列表为空")
        return data.data[0]
    }

    private fun buildTextMsg(r18: Boolean): Pair<String, String?> {
        val data = request(r18)
        val imgUrl = data.urls.original.replace("i.pixiv.cat", Config.plugins.animePic.reverseProxy)
        return Pair(
            MsgUtils.builder()
                .text("标题：${data.title}")
                .text("\nPID：${data.pid}")
                .text("\n作者：${data.author}")
                .text("\n链接：https://www.pixiv.net/artworks/${data.pid}")
                .text("\n反代链接：${imgUrl}")
                .build(),
            imgUrl
        )
    }

    private fun buildPicMsg(url: String?): String {
        url ?: return "图片获取失败了呢 _(:3 」∠)_"
        return MsgUtils.builder().img(url).build()
    }

    private fun recallMsgPic(msgId: Int, bot: Bot) = runBlocking {
        launch {
            delay(Config.plugins.animePic.recallPicTime.times(1000L))
            bot.deleteMsg(msgId)
        }
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long): Pair<String, String?> {
        // 检查是否处于冷却时间
        if (expiringMap[groupId + userId] != null && expiringMap[groupId + userId] == userId) {
            val expectedExpiration = expiringMap.getExpectedExpiration(groupId + userId) / 1000
            throw YuriException("整天色图色图，信不信把你变成色图？冷却：[${expectedExpiration}秒]")
        }
        val r18 = msg.contains(Regex("(?i)r18"))
        if (!Config.plugins.animePic.r18 && r18) throw YuriException("NSFW禁止！")
        val buildTextMsg = buildTextMsg(r18)
        return Pair(buildTextMsg.first, buildTextMsg.second)
    }

    @AnyMessageHandler(cmd = RegexCMD.ANIME_PIC)
    fun animePicHandler(bot: Bot, event: AnyMessageEvent) {
        try {
            val msg = buildMsg(event.message, event.userId, event.groupId)
            bot.sendMsg(event, msg.first, false)
            val cdTime = Config.plugins.animePic.cd.times(1000L)
            expiringMap.put(event.groupId + event.userId, event.userId, cdTime, TimeUnit.MILLISECONDS)
            val picMsgId = bot.sendMsg(event, buildPicMsg(msg.second), false)?.data?.messageId
            if (picMsgId != null) recallMsgPic(picMsgId, bot)
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}