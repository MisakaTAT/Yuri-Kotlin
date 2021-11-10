package com.mikuac.yuri.plugins

import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.CheckUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Poke : BotPlugin() {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    override fun onGroupPokeNotice(bot: Bot, event: PokeNoticeEvent): Int {
        // 检查插件是否禁用
        if (checkUtils.pluginIsDisable(this.javaClass.simpleName)) return MESSAGE_IGNORE
        val baseConfig = ReadConfig.config.base
        val groupId = event.groupId
        val userId = event.userId
        val targetId = event.targetId
        if (event.senderId != baseConfig.botSelfId) {
            if (baseConfig.botSelfId == targetId || baseConfig.adminList.contains(targetId)) {
                bot.sendGroupMsg(groupId, MsgUtils.builder().poke(userId).build(), false)
            }
        }
        return MESSAGE_IGNORE
    }

}




