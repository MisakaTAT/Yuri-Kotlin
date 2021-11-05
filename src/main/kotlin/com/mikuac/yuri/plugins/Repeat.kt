package com.mikuac.yuri.plugins

import cn.hutool.core.util.RandomUtil
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.common.config.ReadConfig
import com.mikuac.yuri.common.utils.CheckUtils
import com.mikuac.yuri.common.utils.LogUtils
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class Repeat : BotPlugin() {

    @Autowired
    private lateinit var checkUtils: CheckUtils

    /**
     * 创建缓存 过期时间内不复读重复内容
     */
    private val expiringMap: ExpiringMap<Long, String> = ExpiringMap.builder()
        .variableExpiration()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(ReadConfig.config.plugin.repeat.waitTime.times(1000L), TimeUnit.MILLISECONDS)
        .build()

    /**
     * 最后一条消息
     */
    private val lastMsgMap: HashMap<Long, String> = HashMap()

    /**
     * 消息统计
     */
    private val countMap: HashMap<Long, Int> = HashMap()

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        // 检查插件是否禁用
        if (checkUtils.pluginIsDisable(this.javaClass.simpleName)) return MESSAGE_IGNORE
        val msg = event.message
        val groupId = event.groupId

        // 过滤指令
        if (msg.startsWith(ReadConfig.config.command.prefix)) return MESSAGE_IGNORE

        // 如果缓存中存在内容则不进行复读
        val cache = expiringMap[groupId]
        if (cache != null && cache == msg) return MESSAGE_IGNORE

        // 获取Map中当前群组最后一条消息内容
        val lastMsg = lastMsgMap.getOrDefault(groupId, "")

        // 获取当前群组同内容消息重复次数
        var count = countMap.getOrDefault(groupId, 0)

        if (msg.equals(lastMsg)) {
            countMap[groupId] = ++count
            if (count == RandomUtil.randomInt(ReadConfig.config.plugin.repeat.thresholdValue)) {
                bot.sendGroupMsg(groupId, msg, false)
                val waitTime = ReadConfig.config.plugin.repeat.waitTime.times(1000L)
                expiringMap.put(groupId, msg, waitTime, TimeUnit.MILLISECONDS)
                countMap[groupId] = 0
                LogUtils.action(event.userId, groupId, this.javaClass.simpleName)
            }
        } else {
            lastMsgMap[groupId] = msg
            countMap[groupId] = 0
        }
        return MESSAGE_IGNORE
    }

}