package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.PrivateMessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.entity.GroupBlackListEntity
import com.mikuac.yuri.entity.GroupWhiteListEntity
import com.mikuac.yuri.entity.UserBlackListEntity
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.repository.GroupBlackListRepository
import com.mikuac.yuri.repository.GroupWhiteListRepository
import com.mikuac.yuri.repository.UserBlackListRepository
import com.mikuac.yuri.utils.CheckUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.regex.Matcher

@Shiro
@Component
class Manager {

    @Autowired
    private lateinit var userBlackListRepository: UserBlackListRepository

    @Autowired
    private lateinit var groupBlackListRepository: GroupBlackListRepository

    @Autowired
    private lateinit var groupWhiteListRepository: GroupWhiteListRepository

    @Autowired
    private lateinit var checkUtils: CheckUtils

    @PrivateMessageHandler(cmd = RegexCMD.MANAGER)
    fun managerHandler(bot: Bot, event: PrivateMessageEvent, matcher: Matcher) {
        if (event.userId !in ReadConfig.config.base.adminList) {
            bot.sendPrivateMsg(event.userId, "此操作仅管理员可执行 config.yaml --> adminList", false)
            return
        }
        val type = matcher.group(1).uppercase(Locale.getDefault())
        val actionType = matcher.group(2).uppercase(Locale.getDefault())
        val action = matcher.group(3).uppercase(Locale.getDefault())
        val target = matcher.group(4).toLong()

        when (type) {
            "GROUP" -> {
                groupActionType(actionType, action, target, bot, event)
            }
            "USER" -> {
                privateActionType(actionType, action, target, bot, event)
            }
        }

    }

    private fun groupActionType(
        actionType: String,
        action: String,
        target: Long,
        bot: Bot,
        event: PrivateMessageEvent
    ) {
        when (actionType) {
            "BLACK" -> {
                groupBlackListAction(action, target, bot, event)
            }
            "WHITE" -> {
                groupWhiteListAction(action, target, bot, event)
            }
        }
    }

    private fun privateActionType(
        actionType: String,
        action: String,
        target: Long,
        bot: Bot,
        event: PrivateMessageEvent
    ) {
        when (actionType) {
            "BLACK" -> {
                privateBlackListAction(action, target, bot, event)
            }
            "WHITE" -> {
                bot.sendPrivateMsg(event.userId, "暂不支持用户白名单操作", false)
            }
        }
    }

    private fun groupBlackListAction(action: String, target: Long, bot: Bot, event: PrivateMessageEvent) {
        when (action) {
            "ADD" -> {
                if (checkUtils.checkGroupInBlackList(target)) {
                    bot.sendPrivateMsg(event.userId, "群组 $target 已处于黑名单中，无需重复添加。", false)
                    return
                }
                groupBlackListRepository.save(GroupBlackListEntity(0, target))
                bot.sendPrivateMsg(event.userId, "群组 $target 已加入黑名单", false)
            }
            "DEL" -> {
                if (!checkUtils.checkGroupInBlackList(target)) {
                    bot.sendPrivateMsg(event.userId, "群组 $target 不在黑名单内，无需删除。", false)
                    return
                }
                groupBlackListRepository.deleteByGroupId(target)
                bot.sendPrivateMsg(event.userId, "群组 $target 已从黑名单移出", false)
            }
        }
    }

    private fun privateBlackListAction(action: String, target: Long, bot: Bot, event: PrivateMessageEvent) {
        when (action) {
            "ADD" -> {
                if (checkUtils.checkUserInBlackList(target)) {
                    bot.sendPrivateMsg(event.userId, "用户 $target 已处于黑名单中，无需重复添加。", false)
                    return
                }
                userBlackListRepository.save(UserBlackListEntity(0, target))
                bot.sendPrivateMsg(event.userId, "用户 $target 已加入黑名单", false)
            }
            "DEL" -> {
                if (!checkUtils.checkUserInBlackList(target)) {
                    bot.sendPrivateMsg(event.userId, "用户 $target 不在黑名单内，无需删除。", false)
                    return
                }
                userBlackListRepository.deleteByUserId(target)
                bot.sendPrivateMsg(event.userId, "用户 $target 已从黑名单移出", false)
            }
        }
    }

    private fun groupWhiteListAction(action: String, target: Long, bot: Bot, event: PrivateMessageEvent) {
        when (action) {
            "ADD" -> {
                if (checkUtils.checkGroupInWhiteList(target)) {
                    bot.sendPrivateMsg(event.userId, "群组 $target 已处于白名单中，无需重复添加。", false)
                    return
                }
                groupWhiteListRepository.save(GroupWhiteListEntity(0, target))
                bot.sendPrivateMsg(event.userId, "群组 $target 已加入白名单", false)
            }
            "DEL" -> {
                if (!checkUtils.checkGroupInWhiteList(target)) {
                    bot.sendPrivateMsg(event.userId, "群组 $target 不在白名单内，无需删除。", false)
                    return
                }
                groupWhiteListRepository.deleteByGroupId(target)
                bot.sendPrivateMsg(event.userId, "群组 $target 已从白名单移出", false)
            }
        }
    }

}