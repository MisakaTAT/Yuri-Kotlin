package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.utils.SendUtils
import lombok.Getter
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
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

    @Getter
    @Suppress("kotlin:S6218")
    data class GroupRouletteData(
        private var bulletCount: Int,
        private val userId: Long,
        private val groupId: Long,
        private val bot: Bot
    ) {
        // 未指定装弹量则默认填充 1 颗
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

        // 当前进度 以 0 作为初始值
        var progress: Int = 0

        // true 代表此处有子弹 false 代表没有，shuffled 对 list 进行 “洗牌”
        val shotList = originShotList[bulletCount - 1].shuffled()

        fun userId(): Long = this.userId
        fun groupId(): Long = this.groupId
        fun bot(): Bot = this.bot
    }

    enum class RouletteType(val operateName: String, val introduction: String) {
        // operateName 在使用指令时用到 introduction 在游戏介绍时用到
        MUTE("禁言", "禁言"),
        KICK("踢人", "踢出"),
    }

    // 使用 groupId 作为 key 储存数据
    private val expiringMap: ExpiringMap<Long, GroupRouletteData> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .asyncExpirationListener { _: Long, v: GroupRouletteData -> expCallBack(v) }
        .build()

    // 超时回调
    private fun expCallBack(v: GroupRouletteData) {
        SendUtils.at(v.userId(), v.groupId(), v.bot(), "当前群组轮盘对决已超时，如需继续请重新发起~")
    }

    // 默认为禁言模式
    private var rouletteType: RouletteType = RouletteType.MUTE

    // 默认的回复语录
    private val defaultQuotations = listOf(
        // 未打中时的回复  0-4
        "呼呼，没有爆裂的声响，你活了下来",
        "虽然黑洞洞的枪口很恐怖，但好在没有子弹射出来，你活下来了",
        "\"咔！\"，你没死，看来运气不错",
        "手别抖.jpg 快把枪拿稳了",
        "笨蛋，你还活着，把眼睛睁开吧",
        // 轮盘介绍  "$1"为当前子弹数 "$2"为当前模式  5
        "左轮一次可填充六颗子弹，本次填充了$1颗子弹，中弹的人将会被$2，如果你准备好了，请发送 开枪 对自己扣动扳机吧。",
        // 手枪卡壳（5% 的概率开枪失败）  6
        "\"咔！\"，伴随着扳机扣动的声响，但是并没有子弹射出来，枪···卡壳了，你因此逃过一劫",
        // 打中时的回复 7
        "中弹后穿越到了异世界···",
        // 没有权限时的回答  8
        "哼哼~ 看起来$1并没有惩罚你的权限呢，这次就先放过你吧，下次可就没这么走运了。"
    )

    private fun changeMode(groupId: Long, userId: Long, userRole: String, bot: Bot) {
        // 只有管理员和群主可以切换轮盘模式
        if (level[userRole]!! > 1) {
            rouletteType = if (rouletteType == RouletteType.MUTE) RouletteType.KICK else RouletteType.MUTE
            bot.sendGroupMsg(groupId, "轮盘已切换至${rouletteType.operateName}模式", false)
            return
        }
        SendUtils.at(userId, groupId, bot, "诶呀~ 切换轮盘模式需要管理员权限呢")
    }

    private fun start(groupId: Long, userId: Long, matcher: Matcher, bot: Bot) {
        if (expiringMap.containsKey(groupId)) return
        // 装弹数
        val bulletCount: Int = matcher.group(1)?.trim()?.toInt() ?: 1
        if (bulletCount == 6) SendUtils.at(
            userId,
            groupId,
            bot,
            "这位群友请不要想不开，如果你执意要这么做的话···"
        )
        expiringMap.put(
            groupId,
            GroupRouletteData(bulletCount, userId, groupId, bot),
            Config.plugins.roulette.timeout.toLong(),
            TimeUnit.SECONDS
        )
        SendUtils.at(
            userId, groupId, bot,
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
        )
    }

    private fun shot(groupId: Long, userId: Long, userRole: String, botRole: String, bot: Bot) {
        if (!expiringMap.containsKey(groupId)) {
            SendUtils.at(userId, groupId, bot, "当前暂无进行时的轮盘，请发送 开始轮盘 创建对局。")
            return
        }
        val data = expiringMap[groupId]!!
        // 重新设置超时
        expiringMap.resetExpiration(groupId)
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
                        RouletteType.MUTE -> {
                            bot.setGroupBan(
                                groupId,
                                userId,
                                (1..Config.plugins.roulette.maxMuteTime).random() * 60
                            )
                        }

                        RouletteType.KICK -> bot.setGroupKick(groupId, userId, false)
                    }
                    defaultQuotations[7]
                } else {
                    defaultQuotations[8].replace("$1", Config.base.nickname)
                }
            }
            expiringMap.remove(groupId)
        } else {
            replyMsg = "${defaultQuotations[(0..4).random()]} (${data.progress + 1}/6)"
        }
        SendUtils.at(userId, groupId, bot, replyMsg)
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
            "开枪" -> shot(groupId, userId, userRole, botRole, bot)
            else -> start(groupId, userId, matcher, bot)
        }
    }

}