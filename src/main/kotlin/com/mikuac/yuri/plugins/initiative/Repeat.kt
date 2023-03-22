package com.mikuac.yuri.plugins.initiative

import cn.hutool.core.util.RandomUtil
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.config.Config
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Shiro
@Component
class Repeat {

    private val cfg = Config.plugins.repeat

    private val expiringMap: ExpiringMap<Long, String> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(cfg.waitTime.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    private val lastMsgMap: HashMap<Long, String> = HashMap()

    private val lastUserMap: HashMap<Long, Long> = HashMap()

    private val msgCountMap: HashMap<Long, Int> = HashMap()

    @GroupMessageHandler
    fun handler(bot: Bot, event: GroupMessageEvent) {
        val msg = event.message
        val groupId = event.groupId
        val userId = event.userId

        // 如果缓存中存在内容则不进行复读
        val cache = expiringMap[groupId]
        if (cache != null && cache == msg) return

        // 如果最后一个用户是同一人则跳过
        if (lastUserMap.getOrDefault(groupId, 0) == userId) return

        var count = msgCountMap.getOrDefault(groupId, 0)
        val lastMsg = lastMsgMap.getOrDefault(groupId, "")

        if (lastMsg == msg) {
            lastMsgMap[groupId] = msg
            lastUserMap[groupId] = userId
            msgCountMap[groupId] = ++count
            if (count == RandomUtil.randomInt(cfg.thresholdValue)) {
                bot.sendGroupMsg(groupId, msg, false)
                val waitTime = cfg.waitTime.times(1000L)
                expiringMap.put(groupId, msg, waitTime, TimeUnit.MILLISECONDS)
                msgCountMap[groupId] = 0
            }
            return
        }

        lastMsgMap[groupId] = msg
        msgCountMap[groupId] = 1
        lastUserMap[groupId] = userId
    }

}