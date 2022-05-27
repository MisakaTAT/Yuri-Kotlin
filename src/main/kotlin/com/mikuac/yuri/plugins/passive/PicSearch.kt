package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.SearchModeUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Shiro
@Component
class PicSearch {

    @Autowired
    private lateinit var sauceNao: SauceNao

    @Autowired
    private lateinit var ascii2d: Ascii2d

    @MessageHandler(cmd = RegexCMD.SAUCE_NAO_SEARCH)
    fun picHandler(bot: Bot, event: WholeMessageEvent) {
        SearchModeUtils.setSearchMode(this.javaClass.simpleName, event.userId, event.groupId, bot)
    }

    @MessageHandler
    fun picSearch(bot: Bot, event: WholeMessageEvent) {
        if (!SearchModeUtils.check(this.javaClass.simpleName, event.userId, event.groupId)) return
        // 发送检索结果
        try {
            val imgUrl = SearchModeUtils.getImgUrl(event.userId, event.groupId, event.arrayMsg)
            if (imgUrl.isNullOrBlank()) return
            val imgMd5 = imgUrl.split("-").last()
            // SauceNao
            val sauceNaoResult = sauceNao.buildMsgForSauceNao(imgUrl, imgMd5)
            bot.sendMsg(event, sauceNaoResult.second, false)

            if (!ReadConfig.config.plugin.picSearch.alwaysUseAscii2d) {
                val similarity = sauceNaoResult.first
                if (similarity.isNotBlank()) {
                    if (similarity.toFloat() > ReadConfig.config.plugin.picSearch.similarityLimit.toFloat()) return
                }
            }

            bot.sendMsg(event,"检索结果相似度较低，正在使用Ascii2d进行检索···",false)
            val ascii2dResult = ascii2d.buildMsgForAscii2d(imgUrl, imgMd5)
            // Ascii2d 色合検索
            bot.sendMsg(event, ascii2dResult.first, false)
            // Ascii2d 特徴検索
            bot.sendMsg(event, ascii2dResult.second, false)
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}