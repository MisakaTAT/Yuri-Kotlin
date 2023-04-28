package com.mikuac.yuri.plugins.initiative

import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.LocalDateTimeUtil
import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.ParseYoutubeDTO
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class ParseYoutube {

    private val cfg = Config.plugins.parseYoutube

    private fun request(url: String): ParseYoutubeDTO {
        val id = RegexUtils.group("id", url, Regex.YOUTUBE_URL)
        if (id.isBlank()) throw YuriException("Youtube Video ID 提取失败")
        val api = "https://youtube.googleapis.com/youtube/v3/videos?part=snippet,statistics&id=${id}&key=${cfg.apiKey}"
        return NetUtils.get(api, true).use { resp ->
            Gson().fromJson(resp.body?.string(), ParseYoutubeDTO::class.java)
        }
    }

    private fun buildMsg(parseYoutube: ParseYoutubeDTO): String {
        if (parseYoutube.items.isEmpty()) throw YuriException("数据获取失败")
        val data = parseYoutube.items[0]
        val img = NetUtils.getBase64(data.snippet.thumbnails.maxres.url, cfg.proxy)
        val localDateTime = LocalDateTimeUtil.parse(data.snippet.publishedAt, "yyyy-MM-dd'T'HH:mm:ss'Z'")
        val time = LocalDateTimeUtil.format(localDateTime, DatePattern.NORM_DATETIME_PATTERN)

        return MsgUtils.builder()
            .img(img)
            .text("\n${ShiroUtils.escape2(data.snippet.title)}")
            .text("\n频道：${ShiroUtils.escape2(data.snippet.channelTitle)} 播放：${data.statistics.viewCount}")
            .text("\n收藏：${data.statistics.favoriteCount} 点赞：${data.statistics.likeCount}")
            .text("\n发布时间：${time}")
            .text("\nhttps://youtube.com/watch?v=${data.id}")
            .build()
    }

    @AnyMessageHandler
    fun handler(bot: Bot, event: AnyMessageEvent) {
        ExceptionHandler.with(bot, event) {
            if (!RegexUtils.match(event.message, Regex.YOUTUBE_URL)) return@with
            bot.sendMsg(event, buildMsg(request(event.message)), false)
        }
    }

}