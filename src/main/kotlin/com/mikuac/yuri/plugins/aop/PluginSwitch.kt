package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.CheckUtils
import com.mikuac.yuri.common.utils.MsgSendUtils
import com.mikuac.yuri.common.utils.RegexUtils
import com.mikuac.yuri.entity.PluginSwitchEntity
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.repository.PluginSwitchRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class PluginSwitch : BotPlugin() {

    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var checkUtils: CheckUtils

    @Autowired
    private lateinit var repository: PluginSwitchRepository

    @PostConstruct
    fun init() {
        val pluginList =
            listOf("Poke", "EroticPic", "HttpCat", "GroupJoinAndQuit", "Repeat", "GithubRepo", "AntiBiliMiniApp")
        pluginList.forEach {
            if (repository.findByPluginName(it).isPresent) {
                log.info { "Plugin switch database table field $it skip." }
            } else {
                log.info { "Plugin switch database table field $it init." }
                repository.save(PluginSwitchEntity(0, it, false))
            }
        }
    }

    private fun check(msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.PLUGIN_SWITCH.value)) return
        if (!checkUtils.roleCheck(userId, groupId, bot)) return
        val pluginName = RegexUtils.group(RegexEnum.PLUGIN_SWITCH.value, 2, msg)
        val action = RegexUtils.group(RegexEnum.PLUGIN_SWITCH.value, 1, msg)
        val plugin = repository.findByPluginName(pluginName)
        if (!plugin.isPresent) {
            MsgSendUtils.atSend(userId, groupId, bot, "插件${pluginName}不存在，请检查指令是否正确！")
            return
        }
        if (dbAction(plugin, action)) MsgSendUtils.atSend(userId, groupId, bot, "插件${pluginName}已${action}用")
    }

    private fun dbAction(plugin: Optional<PluginSwitchEntity>, action: String): Boolean {
        return when (action) {
            "启" -> {
                repository.save(PluginSwitchEntity(plugin.get().id, plugin.get().pluginName, false))
                true
            }
            "停" -> {
                repository.save(PluginSwitchEntity(plugin.get().id, plugin.get().pluginName, true))
                true
            }
            else -> false
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        check(event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        check(event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}
