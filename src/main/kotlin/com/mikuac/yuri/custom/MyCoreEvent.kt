package com.mikuac.yuri.custom

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.CoreEvent
import com.mikuac.yuri.config.Config
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Primary
@Component
class MyCoreEvent : CoreEvent() {

    override fun online(bot: Bot) {
        Config.base.adminList.forEach {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val date = LocalDateTime.now().format(formatter)
            bot.sendPrivateMsg(it, "上线时间 $date", false)
        }
    }

}