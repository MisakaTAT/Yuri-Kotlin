package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.entity.DriftBottleEntity
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.DriftBottleRepository
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher

@Shiro
@Component
class DriftBottle {

    private val cfg = Config.plugins.driftBottle

    private val expiringMap: ExpiringMap<Long, Long> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .build()

    @Autowired
    private lateinit var repository: DriftBottleRepository

    @Suppress("kotlin:S3776")
    @GroupMessageHandler(cmd = Regex.DRIFT_BOTTLE)
    fun handler(event: GroupMessageEvent, bot: Bot, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            val msg = event.message
            val groupId = event.groupId
            val userId = event.userId
            val groupName = bot.getGroupInfo(groupId, false).data.groupName
            val userName = bot.getGroupMemberInfo(groupId, userId, false).data.nickname

            if (msg.startsWith("丢漂流瓶")) {
                val content = matcher.group(1).trim()
                if (content.isEmpty()) throw YuriException("你居然还想丢空瓶子？")
                val res = repository.save(DriftBottleEntity(0, groupId, groupName, userId, userName, content))
                bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder()
                        .at(userId)
                        .text("你将一个编号为 ${res.id} 写着如下内容的纸条塞入瓶中扔进大海，希望有人能够捞到～")
                        .text("\n\n${content}")
                        .build(),
                    false
                )
                return@with
            }

            if (msg.startsWith("捡漂流瓶")) {
                if (expiringMap[groupId] != null && expiringMap[groupId] == userId) {
                    val expectedExpiration = expiringMap.getExpectedExpiration(groupId) / 1000
                    throw YuriException("呜～ 太快了会坏掉的··· 冷却：[${expectedExpiration}秒]")
                }
                expiringMap.put(groupId, userId, cfg.cd.toLong(), TimeUnit.SECONDS)
                val bottles = repository.findAllByOpenIsFalseAndUserIdNotLikeAndGroupIdNotLike(userId, groupId)
                val count = repository.countAllByOpenIsFalse()
                if (bottles.isEmpty()) {
                    throw YuriException("当前剩余 $count 个未被捞取的漂流瓶，你暂无可捞取的漂流瓶（无法捞取本群或自己的瓶子）")
                }
                // Update open state
                val bottle = bottles[Random().nextInt(bottles.size)]
                bottle.open = true
                bottle.openUser = userId
                bottle.openGroup = groupId
                bottle.openUserName = userName
                bottle.openGroupName = groupName
                repository.save(bottle)
                bot.sendGroupMsg(
                    bottle.groupId,
                    MsgUtils.builder()
                        .at(bottle.userId)
                        .text("\n你编号为 ${bottle.id} 的漂流瓶被人捞起来啦~")
                        .text("\n\n群：${groupName}")
                        .text("\n用户：${userName}")
                        .build(),
                    false
                )
                bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder()
                        .at(userId)
                        .text("\n你在海边捡到了一个透明的玻璃瓶，你打开了瓶子，里面写着：\n\n")
                        .text(bottle.content)
                        .text("\n\n群：${bottle.groupName}")
                        .text("\n用户：${bottle.userName}")
                        .build(),
                    false
                )
                return@with
            }

            if (msg.startsWith("跳海")) {
                val count = repository.countAllByOpenIsFalse()
                bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder()
                        .at(userId)
                        .text("\n你缓缓走入大海，感受着海浪轻柔地拍打着你的小腿，膝盖……\n")
                        .text("波浪卷着你的腰腹，你感觉有些把握不住平衡了……\n")
                        .text("……\n")
                        .text("你沉入海中，【${count}】个物体与你一同沉浮。\n")
                        .text("不知何处涌来一股暗流，你失去了意识。")
                        .build(),
                    false
                )
                return@with
            }

            if (msg.startsWith("查漂流瓶")) {
                val id = matcher.group(2).trim().toInt()
                val queryBottle = repository.findById(id)
                if (!queryBottle.isPresent) throw YuriException("未查询到编号为 $id 的漂流瓶")
                bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder()
                        .at(userId)
                        .text("\n${queryBottle.get().content}")
                        .text("\n\n状态：${if (queryBottle.get().open) "被捞起" else "未被捞起"}")
                        .text("\n所属用户：${queryBottle.get().userName}")
                        .text("\n所属群组：${queryBottle.get().groupName}")
                        .text("\n捞起用户：${queryBottle.get().openUserName}")
                        .text("\n捞起群组：${queryBottle.get().openGroupName}")
                        .build(),
                    false
                )
            }
        }
    }

}