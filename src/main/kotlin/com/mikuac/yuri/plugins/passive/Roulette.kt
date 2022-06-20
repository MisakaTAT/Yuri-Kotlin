package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class Roulette {

    private val level = object : HashMap<String, Int>() {
        init {
            put("member", 1)
            put("admin", 2)
            put("owner", 3)
        }
    }

    data class GroupRouletteData(
        private var bulletCount: Int,
    ) {
        init {
            if (bulletCount !in (1..6)) bulletCount = 1
        }

        private val originShotList = arrayOf(
            listOf(true, false, false, false, false, false),
            listOf(true, true, false, false, false, false),
            listOf(true, true, true, false, false, false),
            listOf(true, true, true, true, false, false),
            listOf(true, true, true, true, true, false),
            listOf(true, true, true, true, true, true),
        )

        //当前进度 以 0 作为初始值
        var progress: Int = 0

        // true 代表此处有子弹   false 代表没有
        val shotList = originShotList[bulletCount - 1].shuffled()
    }

    enum class RouletteType(val OperateName: String, val introduction: String) {
        // OperateName 在使用指令时用到 introduction 在游戏介绍时用到
        MUTE("禁言", "禁言"),
        KICK("踢人", "踢出"),
    }

    // 使用 groupId 作为 key 储存数据
    private val dataMap = HashMap<Long, GroupRouletteData>()

    private val currentUser = HashMap<Long, Long>()

    // 默认为禁言模式
    private var rouletteType: RouletteType = RouletteType.MUTE

    // 默认的回复语录
    private val defaultQuotations = listOf(
        // 未打中时的回复  0-4
        "无需退路。",
        "英雄们啊，为这最强大的信念，请站在我们这边。",
        "颤抖吧，在真正的勇敢面前。",
        "哭嚎吧，为你们不堪一击的信念。",
        "现在可没有后悔的余地了。",
        // 轮盘介绍  "$1"为当前子弹数 "$2"为当前模式  5
        "这是一把充满荣耀与死亡的左轮手枪，六个弹槽只有$1颗子弹，中弹的那个人将会被$2。勇敢的战士们啊，扣动你们的扳机吧！",
        // 手枪卡壳（5%的概率开枪失败）  6
        "我的手中的这把武器，找了无数工匠都难以修缮如新。不......不该如此......",
        // 打中时的回复  "@"为在此处 @发送者 7
        "米诺斯英雄们的故事......有喜剧，便也会有悲剧。舍弃了荣耀，@选择回归平凡......",
        // 没有权限时的回答  8
        "听啊，悲鸣停止了。这是幸福的和平到来前的宁静。（无管理员权限或权限低于目标用户）"
    )

    private fun changeMode(groupId: Long, userId: Long, userRole: String, bot: Bot) {
        // 只有管理员和群主可以切换轮盘模式
        if (level[userRole]!! < 1) {
            bot.sendGroupMsg(
                groupId,
                MsgUtils.builder().at(userId).text("诶呀~ 切换轮盘模式需要管理员权限呢").build(),
                false
            )
            return
        }
        rouletteType = if (rouletteType == RouletteType.MUTE) RouletteType.KICK else RouletteType.MUTE
        bot.sendGroupMsg(groupId, "轮盘已切换至${rouletteType.OperateName}模式", false)
    }

    private fun start(groupId: Long, userId: Long, msg: String, bot: Bot) {
        if (dataMap.containsKey(groupId)) return
        val lastChar = msg.trim().last()
        val bulletCount: Int =
            if (lastChar.isDigit() && lastChar.toString().toInt() in (1..6)) lastChar.toString().toInt() else 1
        dataMap[groupId] = GroupRouletteData(bulletCount)
        bot.sendGroupMsg(
            groupId,
            MsgUtils.builder()
                .at(userId)
                .text(
                    defaultQuotations[5].replace(
                        "$1",
                        when (bulletCount) {
                            1 -> "一"
                            2 -> "二"
                            3 -> "三"
                            4 -> "四"
                            5 -> "五"
                            6 -> "六"
                            else -> ""
                        }
                    ).replace("$2", rouletteType.introduction)
                ).build(),
            false
        )
    }

    private fun shot(groupId: Long, userId: Long, userRole: String, botRole: String, bot: Bot) {
        if (!dataMap.containsKey(groupId)) {
            bot.sendGroupMsg(groupId, MsgUtils.builder().at(userId).text("当前暂无进行时的轮盘，请发送 开始轮盘 创建对局。").build(), false)
            return
        }
        // 一个人一次只能开一枪
        if (currentUser[groupId] == userId) {
            bot.sendGroupMsg(groupId, MsgUtils.builder().at(userId).text("您已经开过一枪了，请等候下一位玩家操作。").build(), false)
            return
        }
        val data = dataMap[groupId]!!
        // 最后回复的消息
        val replyMsg: String
        // 判断此处是否有子弹
        if (data.shotList[data.progress]) {
            // 5% 几率枪卡壳
            replyMsg = if ((1..100).random() <= 5) {
                defaultQuotations[6]
            } else {
                if (level[botRole]!! > level[userRole]!!) {
                    when (rouletteType) {
                        RouletteType.MUTE -> bot.setGroupBan(groupId, userId, (1..5).random() * 60)
                        RouletteType.KICK -> bot.setGroupKick(groupId, userId, false)
                    }
                    defaultQuotations[7]
                } else {
                    defaultQuotations[8]
                }
            }
            dataMap.remove(groupId)
        } else {
            replyMsg = "${defaultQuotations[data.progress]}(${data.progress + 1}/6)"
        }
        if (replyMsg.contains("@")) replyMsg.replace("@", MsgUtils.builder().at(userId).build())
        bot.sendGroupMsg(groupId, replyMsg, false)
        currentUser[groupId] = userId
        data.progress++
    }

    @GroupMessageHandler(cmd = RegexCMD.ROULETTE)
    fun rouletteHandler(event: GroupMessageEvent, bot: Bot, matcher: Matcher) {
        val groupId = event.groupId
        val userId = event.userId
        val userRole = bot.getGroupMemberInfo(groupId, event.userId, true).data.role
        val botRole = bot.getGroupMemberInfo(groupId, event.selfId, true).data.role
        when (matcher.group(0)) {
            "切换轮盘模式" -> changeMode(groupId, userId, userRole, bot)
            "开始轮盘" -> start(groupId, userId, event.message, bot)
            "开枪" -> shot(groupId, userId, userRole, botRole, bot)
        }
    }

}