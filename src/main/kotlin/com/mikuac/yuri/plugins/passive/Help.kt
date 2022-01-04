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

    val msg = buildMsg()

    private fun buildMsg(): MutableList<MutableMap<String, Any>>? {
        val msgList = ArrayList<String>()

        val info = """
            Version: Beta
            GitHub: https://github.com/MisakaTAT/Yuri-Kotlin
            Powered By: https://github.com/MisakaTAT/Shiro
        """.trimIndent()
        msgList.add(info)

        val whatAnime = """
            WhatAnime 番剧检索
            
            权限：所有人
            开始指令：搜番 / 找番
            退出指令：结束检索 / 谢谢
        """.trimIndent()
        msgList.add(whatAnime)

        val sauceNao = """
            SauceNao 图片检索

            权限：所有人
            开始指令：搜图 / 识图
            退出指令：结束检索 / 谢谢
        """.trimIndent()
        msgList.add(sauceNao)

        val animePic = """
            AnimePic 色图Time!

            权限：所有人
            指令：色图 / 色图 r18 / setu / setu r18
        """.trimIndent()
        msgList.add(animePic)

        val blackListManager = """
            BlackListManager 用户黑名单管理

            权限：机器人管理员
            指令：ban @user / unban @user
        """.trimIndent()
        msgList.add(blackListManager)

        val botStatus = """
            BotStatus 机器人状态

            权限：所有人
            指令：status / 状态
        """.trimIndent()
        msgList.add(botStatus)

        val bvToAv = """
            BvToAv 哔哩哔哩av/bv号转换
            
            权限：所有人
            指令：bv2av BV1Xx411c7cH / av2bv av120040
        """.trimIndent()
        msgList.add(bvToAv)

        val epicFreeGame = """
            EpicFreeGame 免费游戏
            
            权限：所有人
            指令：epic
        """.trimIndent()
        msgList.add(epicFreeGame)

        val githubRepo = """
            GithubRepo 仓库查询

            权限：所有人
            指令：github MisakaTAT/Shiro
        """.trimIndent()
        msgList.add(githubRepo)

        val httpCat = """
            HttpCat Http状态码猫猫图

            权限：所有人
            指令：httpcat 404
        """.trimIndent()
        msgList.add(httpCat)

        val phoenixWright = """
            PhoenixWright 逆转裁判表情

            权限：所有人
            指令：nzcp 上部文字 下部文字 / 逆转裁判 上部文字 下部文字
        """.trimIndent()
        msgList.add(phoenixWright)

        val throwUser = """
            ThrowUser 丢出一名群友

            权限：所有人
            指令：throw @user / 丢 @user
        """.trimIndent()
        msgList.add(throwUser)

        val tarot = """
            Tarot 塔罗牌抽取
            
            权限：所有人
            指令：tarot / 塔罗牌
        """.trimIndent()
        msgList.add(tarot)

        val hitokoto = """
            Hitokoto 一言
            
            权限：所有人
            指令：hitokoto / 一言
        """.trimIndent()
        msgList.add(hitokoto)

        return ShiroUtils.generateForwardMsg(ReadConfig.config.base.botSelfId, ReadConfig.config.base.botName, msgList)
    }

    @GroupMessageHandler(cmd = RegexCMD.HELP)
    fun helpHandler(bot: Bot, event: GroupMessageEvent) {
        bot.sendGroupForwardMsg(event.groupId, msg)
    }

}