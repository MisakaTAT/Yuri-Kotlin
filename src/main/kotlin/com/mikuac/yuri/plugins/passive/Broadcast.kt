package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.Regex
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Slf4j
@Shiro
@Component
class Broadcast {

    private fun sendGroup(bot: Bot, msg: String) {
        bot.groupList.data.forEach {
            bot.sendGroupMsg(it.groupId, msg, false)
            log.info("广播到群 [${it.groupName}](${it.groupId}) 成功")
        }
    }

    @AnyMessageHandler(cmd = Regex.BROADCAST)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        if (event.userId in Config.base.adminList) {
            val msg = matcher.group(1)
            sendGroup(bot, msg.trim())
            return
        }
        bot.sendMsg(event, "您没有权限执行此操作", false)
    }

}