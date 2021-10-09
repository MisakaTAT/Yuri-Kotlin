package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.CheckUtils
import com.mikuac.yuri.common.utils.MsgSendUtils
import com.mikuac.yuri.common.utils.RegexUtils
import com.mikuac.yuri.entity.PluginSwitchEntity
import com.mikuac.yuri.repository.PluginSwitchRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class PluginSwitch : BotPlugin() {

    private val regex = Regex("^([启停])用插件\\s+(.*)")

    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var checkUtils: CheckUtils

    @Autowired
    private lateinit var repository: PluginSwitchRepository

    @PostConstruct
    fun init() {
        val pluginList = listOf("Poke", "EroticPic", "HttpCat", "GroupJoinAndQuit", "Repeat")
        pluginList.forEach {
            if (repository.findByPluginName(it).isPresent) {
                log.info { "Plugin switch database table field $it skip." }
            } else {
                log.info { "Plugin switch database table field $it init." }
                repository.save(PluginSwitchEntity(0, it, false))
            }
        }
    }

    private fun check(groupId: Long, userId: Long, msg: String, bot: Bot): Boolean {
        if (!checkUtils.roleCheck(userId, groupId, bot)) return false
        val pluginName = RegexUtils.group(regex, 2, msg)
        val action = RegexUtils.group(regex, 1, msg)
        val plugin = repository.findByPluginName(pluginName)
        if (!plugin.isPresent) {
            MsgSendUtils.sendAll(userId, groupId, bot, "插件${pluginName}不存在，请检查指令是否正确！")
            return false
        }
        if (dbAction(plugin, action)) MsgSendUtils.sendAll(userId, groupId, bot, "插件${pluginName}已${action}用")
        log.info { "Plugin $pluginName ${if (action == "启") "enable" else "disable"} - User: $userId" }
        return true
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
        val msg = event.message
        if (msg.matches(regex)) {
            val userId = event.userId
            if (!check(0L, userId, msg, bot)) return MESSAGE_IGNORE
        }
        return MESSAGE_IGNORE;
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            val groupId = event.groupId
            val userId = event.userId
            if (check(groupId, userId, msg, bot)) return MESSAGE_IGNORE
        }
        return MESSAGE_IGNORE
    }

}
