package com.mikuac.yuri.plugins.initiative

import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.LocalDateTimeUtil
import com.alibaba.fastjson2.to
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.ImageUtils
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.RegexUtils
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class ParseYoutube {

    data class ParseYoutube(
        val items: List<Item>,
    ) {
        data class Item(
            val id: String,
            val snippet: Snippet,
            val statistics: Statistics
        ) {
            data class Snippet(
                val channelTitle: String,
                val publishedAt: String,
                val thumbnails: Thumbnails,
                val title: String
            ) {
                data class Thumbnails(
                    val maxres: Maxres,
                ) {
                    data class Maxres(
                        val url: String
                    )
                }
            }

            data class Statistics(
                val favoriteCount: String,
                val likeCount: String,
                val viewCount: String
            )
        }
    }

    private val regex = "^(?:https?://)?(?:www.)?(?:youtube.com|youtu.be)/(?:watch\\?v=)([^#&?]*).*\$".toRegex()

    private fun request(url: String): ParseYoutube {
        val data: ParseYoutube
        val id = RegexUtils.group(regex, 1, url)
        if (id.isBlank()) throw YuriException("Youtube链接解析失败")
        val api = "https://youtube.googleapis.com/youtube/v3/videos?part=snippet," +
                "statistics&id=${id}&key=${Config.plugins.parseYoutube.apiKey}"
        val resp = NetUtils.get(api, true)
        data = resp.body?.string().to<ParseYoutube>()
        resp.close()
        return data
    }

    private fun buildMsg(parseYoutube: ParseYoutube): String {
        if (parseYoutube.items.isEmpty()) throw YuriException("数据获取失败")
        val data = parseYoutube.items[0]
        val img = ImageUtils.formatPNG(
            data.snippet.thumbnails.maxres.url,
            Config.plugins.githubRepo.proxy
        )
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
        try {
            if (!regex.matches(event.message)) return
            bot.sendMsg(event, buildMsg(request(event.message)), false)
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}