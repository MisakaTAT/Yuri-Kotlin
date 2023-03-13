package com.mikuac.yuri.ctx

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotContainer
import com.mikuac.yuri.config.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Ctx {

    @Autowired
    private lateinit var container: BotContainer

    private var botInstance: Bot? = null

    fun bot(): Bot {
        if (botInstance != null) return botInstance as Bot
        botInstance = container.robots[Config.base.selfId]
        return botInstance as Bot
    }

}