package com.mikuac.yuri.common.utils

import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.repository.PluginSwitchRepository
import com.mikuac.yuri.repository.UserBlackListRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CheckUtils {

    @Autowired
    private lateinit var pluginSwitchRepository: PluginSwitchRepository

    @Autowired
    private lateinit var userBlackListRepository: UserBlackListRepository

    fun basicCheck(className: String, userId: Long, groupId: Long, bot: Bot): Boolean {
        if (pluginIsDisable(className, userId, groupId, bot)) return false
        if (checkUserInBlackList(userId, groupId, bot)) return false
        return true
    }

    // 管理员权限检查
    fun roleCheck(userId: Long, groupId: Long, bot: Bot): Boolean {
        if (ReadConfig.config.base.adminList.contains(userId)) return true
        MsgSendUtils.atSend(userId, groupId, bot, "您没有权限执行此操作")
        return false
    }

    // 检查插件是否停用
    fun pluginIsDisable(pluginName: String, userId: Long, groupId: Long, bot: Bot): Boolean {
        if (pluginSwitchRepository.findByPluginName(pluginName).get().disable) {
            MsgSendUtils.atSend(userId, groupId, bot, "此模块已停用")
            return true
        }
        return false
    }

    // 检查插件是否停用
    fun pluginIsDisable(pluginName: String): Boolean {
        if (pluginSwitchRepository.findByPluginName(pluginName).get().disable) return true
        return false
    }

    // 检查用户是否在黑名单中
    fun checkUserInBlackList(userId: Long, groupId: Long, bot: Bot): Boolean {
        if (userBlackListRepository.findByUserId(userId).isPresent) {
            MsgSendUtils.atSend(userId, groupId, bot, "您当前被关小黑屋啦，请联系管理员试试吧～")
            return true
        }
        return false
    }

}