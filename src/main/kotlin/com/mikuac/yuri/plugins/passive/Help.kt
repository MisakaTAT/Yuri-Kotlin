package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.enums.RegexCMD
import org.springframework.stereotype.Component

@Component
class Help : BotPlugin() {

    private fun buildMsg(): MutableList<MutableMap<String, Any>>? {
        val msgList = ArrayList<String>()

        val info = """
            该项目为 YuriBot 的 Kotlin 重构版。
            当前正在缓慢重构中，暂无帮助文档。
            GitHub: https://github.com/MisakaTAT/Yuri-Kotlin
            Powered By: https://github.com/MisakaTAT/Shiro
        """.trimIndent()
        msgList.add(info)

        val whatAnime = """
            WhatAnime 番剧检索
            
            开始指令：搜番，找番
            退出指令：结束检索，谢谢
            
            用法：发送开始指令后随即发送需要检索的番剧截图
        """.trimIndent()
        msgList.add(whatAnime)

        val sauceNao = """
            SauceNao 图片检索
            
            支持 推特/Pixiv/本子 检索
            
            开始指令：搜图，识图
            退出指令：结束检索，谢谢
            
            用法：发送开始指令后随即发送需要检索的图片
        """.trimIndent()
        msgList.add(sauceNao)

        return ShiroUtils.generateForwardMsg(ReadConfig.config.base.botSelfId, ReadConfig.config.base.botName, msgList)
    }

    @GroupMessageHandler(cmd = RegexCMD.HELP)
    fun handler(bot: Bot, event: GroupMessageEvent) {
        bot.sendGroupForwardMsg(event.groupId, buildMsg())
    }

}