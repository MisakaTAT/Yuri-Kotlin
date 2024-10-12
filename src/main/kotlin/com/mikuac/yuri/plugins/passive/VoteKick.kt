package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.ShiroUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.model.ArrayMsg
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.utils.SendUtils
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Shiro
@Component
class VoteKick {

    private data class VoteData(
        val targetId: Long,
        val initiatorId: Long,
        val bot: Bot,
        val agreeVotes: MutableSet<Long> = mutableSetOf(),
        val disagreeVotes: MutableSet<Long> = mutableSetOf(),
        var requiredVotes: Int = Config.plugins.voteKick.requiredVotes
    )

    private val voteMap: ExpiringMap<Long, VoteData> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .asyncExpirationListener { groupId: Long, voteData: VoteData -> expireVote(groupId, voteData) }
        .build()

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = Regex.VOTE_KICK)
    fun handler(event: GroupMessageEvent, bot: Bot) {
        val groupId = event.groupId
        val userId = event.userId
        val message = event.message

        when {
            message.startsWith("发起踢人投票") -> initiateVote(groupId, userId, event.arrayMsg, bot)
            message == "赞成" -> vote(groupId, userId, true, bot)
            message == "反对" -> vote(groupId, userId, false, bot)
            message == "结束投票" -> endVote(groupId, userId, bot)
        }
    }

    private fun initiateVote(groupId: Long, initiatorId: Long, arrayMsg: List<ArrayMsg>, bot: Bot) {
        if (voteMap.containsKey(groupId)) {
            SendUtils.at(initiatorId, groupId, bot, "当前已有正在进行的投票，请等待该投票结束后再发起新的投票。")
            return
        }

        val atList = ShiroUtils.getAtList(arrayMsg)
        if (atList.isEmpty() || atList.size > 1) {
            SendUtils.at(initiatorId, groupId, bot, "请正确@要踢出的成员。")
            return
        }
        val targetId = atList[0]

        val initiatorRole = bot.getGroupMemberInfo(groupId, initiatorId, false).data.role
        val targetRole = bot.getGroupMemberInfo(groupId, targetId, false).data.role

        if (initiatorRole == "member" && targetRole != "member") {
            SendUtils.at(initiatorId, groupId, bot, "你没有权限发起针对管理员或群主的踢人投票。")
            return
        }

        voteMap.put(
            groupId,
            VoteData(targetId, initiatorId, bot),
            Config.plugins.voteKick.timeout.toLong(),
            TimeUnit.MINUTES
        )
        SendUtils.group(
            groupId,
            bot,
            "已发起针对 ${
                MsgUtils.builder().at(targetId).build()
            } 的踢人投票。\n投票持续 ${Config.plugins.voteKick.timeout} 分钟,请发送 “赞成” 或 “反对” 参与。"
        )
    }

    private fun vote(groupId: Long, voterId: Long, isAgree: Boolean, bot: Bot) {
        val voteData = voteMap[groupId]
        if (voteData == null) {
            SendUtils.at(voterId, groupId, bot, "当前没有正在进行的投票。")
            return
        }

        if (voterId == voteData.targetId) {
            SendUtils.at(voterId, groupId, bot, "你不能参与针对自己的投票。")
            return
        }

        val (addedTo, removedFrom) = if (isAgree) {
            voteData.agreeVotes to voteData.disagreeVotes
        } else {
            voteData.disagreeVotes to voteData.agreeVotes
        }

        if (addedTo.add(voterId)) {
            removedFrom.remove(voterId)
            if (!isAgree) {
                voteData.requiredVotes++
            }
            val vt = if (isAgree) "赞成" else "反对"
            SendUtils.group(
                groupId,
                bot,
                "${
                    MsgUtils.builder().at(voterId).build()
                } 已投$vt。当前赞成票数: ${voteData.agreeVotes.size}, 反对票数: ${voteData.disagreeVotes.size}, 需要赞成票数: ${voteData.requiredVotes}"
            )

            if (voteData.agreeVotes.size >= voteData.requiredVotes) {
                executeKick(groupId, voteData, bot)
            }
        } else {
            SendUtils.at(voterId, groupId, bot, "你已经参与过投票了。")
        }
    }

    private fun endVote(groupId: Long, userId: Long, bot: Bot) {
        val voteData = voteMap[groupId]
        if (voteData == null) {
            SendUtils.at(userId, groupId, bot, "当前没有正在进行的投票。")
            return
        }

        if (userId != voteData.initiatorId) {
            val userRole = bot.getGroupMemberInfo(groupId, userId, false).data.role
            if (userRole != "admin" && userRole != "owner") {
                SendUtils.at(userId, groupId, bot, "只有投票发起人、管理员或群主可以结束投票。")
                return
            }
        }

        voteMap.remove(groupId)
        SendUtils.group(
            groupId,
            bot,
            "投票已被手动结束。最终结果: 赞成票数: ${voteData.agreeVotes.size}, 反对票数: ${voteData.disagreeVotes.size}, 需要赞成票数: ${voteData.requiredVotes}"
        )
    }

    private fun expireVote(groupId: Long, voteData: VoteData) {
        voteMap.remove(groupId)
        SendUtils.group(
            groupId,
            voteData.bot,
            "针对 @${voteData.targetId} 的踢人投票已超时。最终结果: 赞成票数: ${voteData.agreeVotes.size}, 反对票数: ${voteData.disagreeVotes.size}, 需要赞成票数: ${voteData.requiredVotes}"
        )
    }

    private fun executeKick(groupId: Long, voteData: VoteData, bot: Bot) {
        voteMap.remove(groupId)
        bot.setGroupKick(groupId, voteData.targetId, false)
        SendUtils.group(
            groupId,
            bot,
            "投票通过。${
                MsgUtils.builder().at(voteData.targetId).build()
            } 已被踢出群聊。最终结果: 赞成票数: ${voteData.agreeVotes.size}, 反对票数: ${voteData.disagreeVotes.size}"
        )
    }
}