package com.mikuac.yuri.plugins.passive

import com.google.common.util.concurrent.RateLimiter
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.ReadConfig
import com.mikuac.yuri.entity.DriftBottleEntity
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.repository.DriftBottleRepository
import com.mikuac.yuri.utils.MsgSendUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.*
import java.util.regex.Matcher

@Shiro
@Component
@Suppress("UnstableApiUsage")
class DriftBottle : ApplicationRunner {

    private lateinit var rateLimiter: RateLimiter

    private val enableLimiter = ReadConfig.config.plugin.animeCrawler.enableLimiter

    private val permitsPerMinute = ReadConfig.config.plugin.driftBottle.permitsPerMinute.toDouble()

    override fun run(args: ApplicationArguments?) {
        if (enableLimiter) {
            rateLimiter = RateLimiter.create(permitsPerMinute / 60)
            log.info("${this.javaClass.simpleName} 已开启调用限速")
        }
    }

    @Autowired
    private lateinit var repository: DriftBottleRepository

    @GroupMessageHandler(cmd = RegexCMD.DRIFT_BOTTLE)
    fun driftBottleHandler(event: GroupMessageEvent, bot: Bot, matcher: Matcher) {
        try {
            if (enableLimiter && !rateLimiter.tryAcquire()) throw YuriException("呜～ 太快了，会坏掉的···")
            val msg = event.message
            val groupId = event.groupId
            val userId = event.userId

            if (msg.startsWith("丢漂流瓶")) {
                val content = matcher.group(1).trim()
                if (content.isEmpty()) throw YuriException("你居然还想丢空瓶子？")
                val groupName = bot.getGroupInfo(groupId, false).data.groupName
                val userName = bot.getGroupMemberInfo(groupId, userId, false).data.nickname
                repository.save(DriftBottleEntity(0, groupId, groupName, userId, userName, content, false))
                bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder()
                        .at(userId)
                        .text("\n你将一个写着 $content 的纸条塞入瓶中扔进大海，希望有人能够捞到～")
                        .build(),
                    false
                )
                return
            }

            if (msg.startsWith("捡漂流瓶")) {
                val bottles = repository.findAllByOpenIsFalseAndUserIdNotLike(event.userId)
                if (bottles.isEmpty()) {
                    throw YuriException("当前无漂流瓶可捞起或仅有你自己的漂流瓶")
                }
                // Update open state
                val bottle = bottles[Random().nextInt(bottles.size)]
                bottle.open = true
                repository.save(bottle)
                bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder()
                        .at(userId)
                        .text("\n你在海边捡到了一个透明的玻璃瓶，你打开了瓶子，里面写着：\n")
                        .text(bottle.content)
                        .text("\n该瓶子来自群：${bottle.groupName}（${bottle.groupId}）")
                        .text("\n该瓶子来自用户：${bottle.userName}（${bottle.userId}）")
                        .build(),
                    false
                )
                return
            }

            if (msg.startsWith("跳海")) {
                val count = repository.countAllByOpenIsFalse()
                bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder()
                        .at(event.userId)
                        .text("\n你缓缓走入大海，感受着海浪轻柔地拍打着你的小腿，膝盖……\n")
                        .text("波浪卷着你的腰腹，你感觉有些把握不住平衡了……\n")
                        .text("……\n")
                        .text("你沉入海中，【${count}】个物体与你一同沉浮。\n")
                        .text("不知何处涌来一股暗流，你失去了意识。")
                        .build(),
                    false
                )
                return
            }
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }

    }

}