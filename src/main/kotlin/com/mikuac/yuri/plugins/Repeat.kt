package com.mikuac.yuri.plugins

import cn.hutool.cache.CacheUtil
import cn.hutool.cache.impl.TimedCache
import cn.hutool.core.util.RandomUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.log.Slf4j.Companion.log
import org.springframework.stereotype.Component


@Component
class Repeat : BotPlugin() {

    /**
     * 创建缓存，1分钟内不复读同一内容
     */
    private val timedCache: TimedCache<Long, String> = CacheUtil.newTimedCache(60 * 1000L)

    /**
     * 最后一条消息
     */
    private val lastMsgMap: HashMap<Long, String> = HashMap()

    /**
     * 消息统计
     */
    private val countMap: HashMap<Long, Int> = HashMap()

    val thresholdValue = RandomUtil.randomInt(ReadConfig.config.plugin.repeat.thresholdValue)

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        val groupId = event.groupId

        // 过滤指令
        if (msg.startsWith(ReadConfig.config.command.prefix)) return MESSAGE_IGNORE

        // 如果缓存中存在内容则不进行复读
        val cache = timedCache.get(groupId, false)
        if (cache != null && cache.equals(msg)) return MESSAGE_IGNORE

        // 获取Map中当前群组最后一条消息内容
        val lastMsg = lastMsgMap.getOrDefault(groupId, "")

        // 获取当前群组同内容消息重复次数
        var count = countMap.getOrDefault(groupId, 0)

        if (msg.equals(lastMsg)) {
            countMap[groupId] = ++count
            if (count == thresholdValue) {
                bot.sendGroupMsg(groupId, msg, false)
                timedCache.put(groupId, msg)
                countMap[groupId] = 0
                log.info("复读成功，复读内容：${msg}")
            }
        } else {
            lastMsgMap[groupId] = msg
            countMap[groupId] = 0
        }
        return MESSAGE_IGNORE
    }

}