package com.mikuac.yuri.exception

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent

object ExceptionHandler {

    fun with(bot: Bot, event: Any, block: () -> Unit) {
        try {
            block()
        } catch (e: YuriException) {
            e.message?.let { push(event, bot, it) }
        } catch (e: Exception) {
            push(event, bot, "ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun push(event: Any, bot: Bot, message: String) {
        when (event) {
            is AnyMessageEvent -> event.let { bot.sendMsg(it, message, false) }
            is GroupMessageEvent -> event.let { bot.sendGroupMsg(it.groupId, message, false) }
            is PrivateMessageEvent -> event.let { bot.sendPrivateMsg(it.userId, message, false) }
        }
    }

}