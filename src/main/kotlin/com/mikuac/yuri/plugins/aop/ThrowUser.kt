package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.bean.MsgChainBean
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.enums.RegexEnum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ThrowUser : BotPlugin() {

    @Autowired
    private lateinit var env: Environment

    private fun buildMsg(atUser: String, groupId: Long, bot: Bot) {
        val port = env.getProperty("local.server.port")
        val msg = MsgUtils.builder().img("http://localhost:${port}/throwUser?qq=${atUser}").build()
        bot.sendGroupMsg(groupId, msg, false)
    }

    private fun handler(msg: String, groupId: Long, arrayMsg: List<MsgChainBean>, bot: Bot) {
        if (!msg.matches(RegexEnum.THROW_USER.value)) return
        val atUser = arrayMsg.filter { "at" == it.type }[0].data["qq"] ?: return
        buildMsg(atUser, groupId, bot)
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        handler(event.message, event.groupId, event.arrayMsg, bot)
        return MESSAGE_IGNORE
    }

}