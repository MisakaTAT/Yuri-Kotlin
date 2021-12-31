package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ThrowUser : BotPlugin() {

    @Autowired
    private lateinit var env: Environment

    private fun buildMsg(event: GroupMessageEvent): String {
        val atList = event.arrayMsg.filter { "at" == it.type }
        if (atList.isEmpty()) throw YuriException("请 @ 一名群成员")
        val atUserId = atList[0].data["qq"]
        if ("all" == atUserId) throw YuriException("哼哼～ 没想到你个笨蛋还想把所有人都丢出去")
        val port = env.getProperty("local.server.port")
        return MsgUtils.builder().img("http://localhost:${port}/throwUser?qq=${atUserId}").build()
    }

    @GroupMessageHandler(cmd = RegexCMD.THROW_USER)
    fun handler(bot: Bot, event: GroupMessageEvent) {
        try {
            val msg = buildMsg(event)
            bot.sendGroupMsg(event.groupId, msg, false)
        } catch (e: YuriException) {
            bot.sendGroupMsg(event.groupId, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}