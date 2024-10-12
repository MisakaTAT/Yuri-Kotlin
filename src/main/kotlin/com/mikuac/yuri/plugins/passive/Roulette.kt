package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.Regex
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

    private val level = mapOf(
        "member" to 1,
        "admin" to 2,
        "owner" to 3
    )

    @Getter
    @Suppress("kotlin:S6218")
    data class GroupRouletteData(
        private val bulletCount: Int,
        private val groupId: Long,
        private val bot: Bot
    ) {
        val shotList = List(6) { it < bulletCount }.shuffled()
        var progress: Int = 0
        var hitCount: Int = 0
        val participants = mutableListOf<Long>()

        fun groupId(): Long = this.groupId
        fun bot(): Bot = this.bot

        fun getSummary(isTimeout: Boolean): String {
            val msg = MsgUtils.builder()

            msg.text(if (isTimeout) "游戏超时！" else "游戏结束！")
            msg.text("\n参与者：")
            participants.forEachIndexed { _, id ->
                msg.at(id)
            }
            msg.text("\n中弹情况：")
            shotList.forEachIndexed { index, shot ->
                msg.text("第${index + 1}发：")
                msg.text(if (shot) "有子弹" else "无子弹")
                if (index < shotList.size - 1) msg.text(", ")
            }
            if (isTimeout && progress < 6) {
                msg.text("\n注意：游戏超时，只进行了 $progress 轮。")
            }
            return msg.build()
        }
    }

    enum class RouletteType(val operateName: String, val introduction: String) {
        MUTE("禁言", "禁言"),
        KICK("踢人", "踢出"),
    }

    private val expiringMap: ExpiringMap<Long, GroupRouletteData> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .asyncExpirationListener { _: Long, v: GroupRouletteData -> expCallBack(v) }
        .build()

    private fun expCallBack(v: GroupRouletteData) {
        SendUtils.group(v.groupId(), v.bot(), v.getSummary(true))
    }

    private var rouletteType: RouletteType = RouletteType.MUTE


    private val defaultQuotations = listOf(
        "看来幸运女神眷顾你，这次安全了。",
        "虽然黑洞洞的枪口很恐怖，但好在没有子弹射出来，你活下来了",
        "\"咔！\"，你没死，看来运气不错",
        "砰！...虚惊一场，原来是空包弹。",
        "命运给了你一次机会，这一枪是空的。",
        "左轮一次可填充六颗子弹，本次填充了$1颗子弹，中弹的人将会被$2，游戏将进行6轮。如果你准备好了，请发送 开枪 对自己扣动扳机吧。",
        "\"咔！\"，伴随着扳机扣动的声响，但是并没有子弹射出来，枪···卡壳了，你因此逃过一劫",
        "砰！中弹了。你被$1了，game over!",
        "哼哼~ 看起来$1并没有惩罚你的权限呢，这次就先放过你吧，下次可就没这么走运了。"
    )

    private fun changeMode(groupId: Long, userId: Long, userRole: String, bot: Bot) {
        if ((level[userRole] ?: 0) > 1) {
            rouletteType = if (rouletteType == RouletteType.MUTE) RouletteType.KICK else RouletteType.MUTE
            bot.sendGroupMsg(groupId, "轮盘已切换至${rouletteType.operateName}模式", false)
            return
        }
        SendUtils.at(userId, groupId, bot, "诶呀~ 切换轮盘模式需要管理员权限呢")
    }

    private fun start(groupId: Long, userId: Long, matcher: Matcher, bot: Bot) {
        if (expiringMap.containsKey(groupId)) return
        val bulletCount: Int = matcher.group(1)?.trim()?.toInt() ?: 1
        if (bulletCount == 6) SendUtils.at(
            userId,
            groupId,
            bot,
            "这位群友请不要想不开，如果你执意要这么做的话···"
        )
        expiringMap.put(
            groupId,
            GroupRouletteData(bulletCount, groupId, bot),
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

    private fun handleInvalidGame(groupId: Long, userId: Long, bot: Bot) {
        SendUtils.at(userId, groupId, bot, "当前暂无进行时的轮盘，请发送 开始轮盘 创建对局。")
    }

    private fun updateShotData(
        data: GroupRouletteData,
        userId: Long,
        botRole: String,
        userRole: String,
        bot: Bot
    ): String {
        val shot = data.shotList[data.progress]
        if (!shot) {
            // 未击中目标
            data.progress++
            return "${defaultQuotations[(0..4).random()]} (${data.progress}/6)"
        }

        // 击中目标
        val isFatal = (1..100).random() <= 5
        if (isFatal) return defaultQuotations[6]

        val userRoleLevel = level[userRole] ?: 0
        val botRoleLevel = level[botRole] ?: 0

        if (botRoleLevel > userRoleLevel) {
            when (rouletteType) {
                RouletteType.MUTE -> {
                    bot.setGroupBan(
                        data.groupId(),
                        userId,
                        (1..Config.plugins.roulette.maxMuteTime).random() * 60
                    )
                }

                RouletteType.KICK -> bot.setGroupKick(data.groupId(), userId, false)
            }
            data.progress++
            data.hitCount++  // 增加击中次数
            return defaultQuotations[7].replace("$1", rouletteType.operateName)
        }

        data.progress++
        data.hitCount++  // 增加击中次数
        return defaultQuotations[8].replace("$1", Config.base.nickname)
    }

    private fun checkGameEnd(data: GroupRouletteData, bot: Bot, groupId: Long) {
        // 游戏结束条件：
        // 1. 进度（回合数）达到最大值（6）
        // 2. 击中次数等于装填的子弹数
        if (data.progress < 6 && data.hitCount < data.shotList.count { it }) return
        val summary = data.getSummary(false)
        SendUtils.group(groupId, bot, summary)
        expiringMap.remove(groupId)
    }

    private fun shot(groupId: Long, userId: Long, userRole: String, botRole: String, bot: Bot) {
        val data = expiringMap[groupId] ?: run {
            handleInvalidGame(groupId, userId, bot)
            return
        }
        expiringMap.resetExpiration(groupId)

        if (!data.participants.contains(userId)) {
            data.participants.add(userId)
        }

        val replyMsg = updateShotData(data, userId, botRole, userRole, bot)
        SendUtils.at(userId, groupId, bot, replyMsg)
        checkGameEnd(data, bot, groupId)
    }

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = Regex.ROULETTE)
    fun handler(event: GroupMessageEvent, bot: Bot, matcher: Matcher) {
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