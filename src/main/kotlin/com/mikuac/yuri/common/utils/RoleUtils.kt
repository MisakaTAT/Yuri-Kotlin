package com.mikuac.yuri.common.utils

import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.common.config.ReadConfig

class RoleUtils {
    companion object {

        fun roleCheck(userId: Long, groupId: Long, bot: Bot): Boolean {
            if (ReadConfig.config?.base?.adminList?.contains(userId) == true) return true
            MsgSendUtils.sendAll(userId, groupId, bot, "您没有权限执行此操作")
            return false
        }

    }
}