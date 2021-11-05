package com.mikuac.yuri.plugins.aop

import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.*
import com.mikuac.yuri.dto.SauceNaoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SauceNao : BotPlugin() {

    private val setRegex = Regex("^[搜识找識]([索别])?[图圖本推]([子特])?(模式)?")

    private val unsetRegex = Regex("^退出[搜识找識]([索别])?[图圖本推]([子特])?(模式)?")

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private fun request(imgUrl: String): SauceNaoDto {
        val key = ReadConfig.config.plugin.sauceNao.key
        val api = "https://saucenao.com/search.php?api_key=${key}&output_type=2&numres=3&db=999&url=${imgUrl}"
        val result = RequestUtils.get(api)
        val json = Gson().fromJson(result, SauceNaoDto::class.java)
        if (json.header.longRemaining <= 0) throw Exception("今日的搜索配额已耗尽啦，明天再来吧～")
        if (json.header.shortRemaining <= 0) throw Exception("短时间内搜索配额已耗尽，休息会再试试吧 _(:зゝ∠)_")
        if (json.results.isEmpty()) throw Exception("非常抱歉，未能找到相似的内容。")
        return json
    }

    private fun check(mode: String, msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!SearchModeUtils.check(setRegex, unsetRegex, msg, mode, userId, groupId, bot)) return
        if (!checkUtils.basicCheck(this.javaClass.simpleName, userId, groupId, bot)) return
        buildMsg(msg, userId, groupId, bot)
        LogUtils.action(userId, groupId, this.javaClass.simpleName)
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            // 重新设置过期时间
            SearchModeUtils.resetExpiration(userId, groupId)
            val imgUrl = RegexUtils.group(Regex("^\\[CQ:image(.*)url=(.*)]"), 2, msg)
            // 返回的结果按相识度排序，第一个相似度最高，默认取第一个
            val result = request(imgUrl).results.filter {
                it.header.indexId in listOf(5, 18, 38, 41)
            }[0]
            val header = result.header
            val data = result.data
            // 构建消息
            val sendMsg = MsgUtils.builder()
                .img(header.thumbnail)
                .text("\n相似度：${header.similarity}%")
            when (header.indexId) {
                5 -> {
                    sendMsg.text("\n标题：${data.title}")
                    sendMsg.text("\n画师：${data.authorName}")
                    sendMsg.text("\n作品主页：https://pixiv.net/i/${data.pixivId}")
                    sendMsg.text("\n画师主页：https://pixiv.net/u/${data.authorId}")
                    sendMsg.text("\n反代地址：https://i.loli.best/${data.pixivId}")
                    sendMsg.text("\n数据来源：SauceNao (Pixiv)")
                }
                41 -> {
                    sendMsg.text("\n链接：${data.extUrls[0]}")
                    sendMsg.text("\n用户：" + "https://twitter.com/${data.twitterUserHandle}")
                    sendMsg.text("\n数据来源：SauceNao (Twitter)")
                }
                in listOf(18, 38) -> {
                    sendMsg.text("\n来源：${data.source}")
                    sendMsg.text("\n日文名：${data.jpName}")
                    sendMsg.text("\n英文名：${data.engName}")
                    sendMsg.text("\n数据来源：SauceNao (H-Misc)")
                }
            }
            MsgSendUtils.send(userId, groupId, bot, sendMsg.build())
        } catch (e: Exception) {
            MsgSendUtils.atSend(userId, groupId, bot, "SauceNao检索失败 ${e.message}")
            LogUtils.debug("${DateUtils.getTime()} ${this.javaClass.simpleName} Exception")
            LogUtils.debug(e.stackTraceToString())
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        check(this.javaClass.simpleName, event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(this.javaClass.simpleName, event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}