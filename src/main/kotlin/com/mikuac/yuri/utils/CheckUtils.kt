package com.mikuac.yuri.utils

import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.repository.GroupBlackListRepository
import com.mikuac.yuri.repository.GroupWhiteListRepository
import com.mikuac.yuri.repository.UserBlackListRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CheckUtils {

    private val cfg = Config.base

    @Autowired
    private lateinit var userBlack: UserBlackListRepository

    @Autowired
    private lateinit var groupBlack: GroupBlackListRepository

    @Autowired
    private lateinit var groupWhite: GroupWhiteListRepository

    // 管理员权限检查
    fun roleCheck(userId: Long, groupId: Long, bot: Bot): Boolean {
        if (cfg.adminList.contains(userId)) return true
        SendUtils.at(userId, groupId, bot, "您没有权限执行此操作")
        return false
    }

    // 检查用户是否在黑名单中
    fun checkUserInBlackList(userId: Long): Boolean {
        if (userBlack.findByUserId(userId).isPresent) return true
        return false
    }

    // 检查群组是否在黑名单中
    fun checkGroupInBlackList(groupId: Long): Boolean {
        if (groupBlack.findByGroupId(groupId).isPresent) return true
        return false
    }

    // 检查群组是否在白名单中
    fun checkGroupInWhiteList(groupId: Long): Boolean {
        if (groupWhite.findByGroupId(groupId).isPresent) return true
        return false
    }

}