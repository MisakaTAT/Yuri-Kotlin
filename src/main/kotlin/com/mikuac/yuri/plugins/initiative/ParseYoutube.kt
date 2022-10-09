package com.mikuac.yuri.plugins.initiative

import cn.hutool.core.date.DatePattern
import cn.hutool.core.date.LocalDateTimeUtil
import com.google.gson.Gson
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.bean.dto.ParseYoutubeDto
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.ImageUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component

@Shiro
@Component
class ParseYoutube {

    private val regex = "^(?:https?://)?(?:www.)?(?:youtube.com|youtu.be)/(?:watch\\?v=)([^#&?]*).*\$".toRegex()

    private fun request(url: String): ParseYoutubeDto {
        val data: ParseYoutubeDto
        try {
            val id = RegexUtils.group(regex, 1, url)
            if (id.isBlank()) throw YuriException("Youtube链接解析失败")
            val api = "https://youtube.googleapis.com/youtube/v3/videos?part=snippet," +
                    "statistics&id=${id}&key=${Config.plugins.parseYoutube.apiKey}"
            val resp = NetUtils.get(api, true)
            data = Gson().fromJson(resp.body?.string(), ParseYoutubeDto::class.java)
            resp.close()
        } catch (e: Exception) {
            throw YuriException("Youtube数据获取异常：${e.message}")
        }
        return data
    }

    private fun buildMsg(parseYoutubeDto: ParseYoutubeDto): String {
        if (parseYoutubeDto.items.isEmpty()) throw YuriException("数据获取失败")
        val data = parseYoutubeDto.items[0]
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

    @MessageHandler
    fun handler(bot: Bot, event: WholeMessageEvent) {
        try {
            if (!regex.matches(event.message)) return
            bot.sendMsg(event, buildMsg(request(event.message)), false)
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}