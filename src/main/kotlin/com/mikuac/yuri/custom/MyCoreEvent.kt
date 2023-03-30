package com.mikuac.yuri.custom

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.CoreEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.utils.DateUtils
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Primary
@Component
class MyCoreEvent : CoreEvent() {

    override fun online(bot: Bot) {
        Config.base.adminList.forEach {
            bot.sendPrivateMsg(it, "上线时间 ${DateUtils.format(LocalDateTime.now())}", false)
        }
    }

}