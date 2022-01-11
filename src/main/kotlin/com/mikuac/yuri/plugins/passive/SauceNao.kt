package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.bean.MsgChainBean
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.dto.SauceNaoDto
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.RequestUtils
import com.mikuac.yuri.utils.SearchModeUtils
import org.springframework.stereotype.Component

@Component
class SauceNao : BotPlugin() {

    @Synchronized
    private fun request(imgUrl: String): SauceNaoDto {
        val key = ReadConfig.config.plugin.sauceNao.key
        val api = "https://saucenao.com/search.php?api_key=${key}&output_type=2&numres=3&db=999&url=${imgUrl}"
        val result = RequestUtils.get(api) ?: throw YuriException("SauceNao API请求失败")
        val json = Gson().fromJson(result.string(), SauceNaoDto::class.java)
        if (json.header.longRemaining <= 0) throw YuriException("今日的搜索配额已耗尽啦")
        if (json.header.shortRemaining <= 0) throw YuriException("短时间内搜索配额已耗尽")
        if (json.results.isEmpty()) throw YuriException("未能找到相似的内容")
        return json
    }

    private fun buildMsg(userId: Long, groupId: Long, arrMsg: List<MsgChainBean>): String? {
        // 重新设置过期时间
        SearchModeUtils.resetExpiration(userId, groupId)
        val images = arrMsg.filter { "image" == it.type }
        if (images.isEmpty()) return null
        val imgUrl = images[0].data["url"] ?: return null
        // 返回的结果按相识度排序，第一个相似度最高，默认取第一个
        val result = request(imgUrl).results.filter {
            it.header.indexId in listOf(5, 18, 38, 41)
        }[0]
        val header = result.header
        val data = result.data
        // 构建消息
        val msg = MsgUtils.builder()
            .img(header.thumbnail)
            .text("\n相似度：${header.similarity}%")
        when (header.indexId) {
            5 -> {
                msg.text("\n标题：${data.title}")
                msg.text("\n画师：${data.authorName}")
                msg.text("\n作品主页：https://pixiv.net/i/${data.pixivId}")
                msg.text("\n画师主页：https://pixiv.net/u/${data.authorId}")
                msg.text("\n反代地址：https://i.loli.best/${data.pixivId}")
                msg.text("\n数据来源：SauceNao (Pixiv)")
            }
            41 -> {
                msg.text("\n链接：${data.extUrls[0]}")
                msg.text("\n用户：" + "https://twitter.com/${data.twitterUserHandle}")
                msg.text("\n数据来源：SauceNao (Twitter)")
            }
            in listOf(18, 38) -> {
                msg.text("\n来源：${data.source}")
                msg.text("\n日文名：${data.jpName}")
                msg.text("\n英文名：${data.engName}")
                msg.text("\n数据来源：SauceNao (H-Misc)")
            }
        }
        return msg.build()
    }

    @MessageHandler(cmd = RegexCMD.SAUCE_NAO_SEARCH)
    fun sauceNaoHandler(bot: Bot, event: WholeMessageEvent) {
        SearchModeUtils.setSearchMode(this.javaClass.simpleName, event.userId, event.groupId, bot)
    }

    @MessageHandler
    fun sauceNaoSearch(bot: Bot, event: WholeMessageEvent) {
        if (!SearchModeUtils.check(this.javaClass.simpleName, event.userId, event.groupId)) return
        // 发送检索结果
        try {
            val msg = buildMsg(event.userId, event.groupId, event.arrayMsg) ?: return
            bot.sendMsg(event, msg, false)
        } catch (e: YuriException) {
            bot.sendMsg(event, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}