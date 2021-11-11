package com.mikuac.yuri.common.utils

import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.repository.GroupBlackListRepository
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

    @Autowired
    private lateinit var groupBlackListRepository: GroupBlackListRepository

    // 管理员权限检查
    fun roleCheck(userId: Long, groupId: Long, bot: Bot): Boolean {
        if (ReadConfig.config.base.adminList.contains(userId)) return true
        MsgSendUtils.atSend(userId, groupId, bot, "您没有权限执行此操作")
        return false
    }

    // 检查插件是否停用
    fun pluginIsDisable(pluginName: String, userId: Long, groupId: Long, bot: Bot): Boolean {
        val result = pluginSwitchRepository.findByPluginName(pluginName)
        if (!result.isPresent) return false
        if (result.get().disable) {
            MsgSendUtils.atSend(userId, groupId, bot, "此模块已停用")
            return true
        }
        return false
    }

    // 检查插件是否停用
    fun pluginIsDisable(pluginName: String): Boolean {
        val result = pluginSwitchRepository.findByPluginName(pluginName)
        if (!result.isPresent) return false
        if (result.get().disable) return true
        return false
    }

    // 检查用户是否在黑名单中
    fun checkUserInBlackList(userId: Long): Boolean {
        if (userBlackListRepository.findByUserId(userId).isPresent) return true
        return false
    }

    // 检查群组是否在黑名单中
    fun checkGroupInBlackList(groupId: Long): Boolean {
        if (groupBlackListRepository.findByGroupId(groupId).isPresent) return true
        return false
    }

}