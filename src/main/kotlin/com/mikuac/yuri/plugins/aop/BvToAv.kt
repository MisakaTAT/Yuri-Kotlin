package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.MsgSendUtils
import com.mikuac.yuri.common.utils.RegexUtils
import mu.KotlinLogging
import org.springframework.stereotype.Component
import kotlin.math.pow


@Component
class BvToAv : BotPlugin() {

    private val regex = Regex("^(?i)bv[2转]av\\s(.*)|^(?i)av[2转]bv\\s(.*)")

    private val log = KotlinLogging.logger {}

    // 算法来源 https://www.zhihu.com/question/381784377/answer/1099438784
    private val table = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF"
    private val s = intArrayOf(11, 10, 3, 8, 4, 6)
    private val xor = 177451812L
    private val add = 8728348608L

    private val bv2avMap: HashMap<String, Int> = HashMap()
    private val av2bvMap: HashMap<Int, String> = HashMap()

    private fun bv2av(bvId: String): String {
        var r: Long = 0
        for (i in 0..57) {
            bv2avMap[table[i].toString()] = i
        }
        for (i in 0..5) {
            r += (bv2avMap[bvId.substring(s[i], s[i] + 1)]!! * 58.0.pow(i.toDouble())).toLong()
        }
        return "av" + (r - add xor xor)
    }

    private fun av2bv(avId: String): String {
        var aid = avId.split("av".toRegex()).toTypedArray()[1].toLong()
        val stringBuilder = StringBuilder("BV1  4 1 7  ")
        aid = (aid xor xor) + add
        for (i in 0..57) {
            av2bvMap[i] = table[i].toString()
        }
        for (i in 0..5) {
            val r = av2bvMap[(aid / 58.0.pow(i.toDouble()) % 58).toInt()]
            stringBuilder.replace(s[i], s[i] + 1, r)
        }
        return stringBuilder.toString()
    }

    private fun action(userId: Long, groupId: Long, bot: Bot, msg: String) {
        try {
            val bvId = RegexUtils.group(regex, 1, msg)
            val avId = RegexUtils.group(regex, 2, msg)
            if (bvId.isNotEmpty()) {
                if (!bvId.matches(Regex("^(?i)BV(.*)"))) {
                    MsgSendUtils.sendAll(userId, groupId, bot, "BV号格式化不正确，请检查后重试。")
                    return
                }
                val aid = bv2av(bvId)
                MsgSendUtils.sendAll(userId, groupId, bot, aid)
                log.info { "BVID to AVID - BVID: $bvId AVID: $aid" }
            }
            if (avId.isNotEmpty()) {
                if (!avId.matches(Regex("^(?i)AV(.*)"))) {
                    MsgSendUtils.sendAll(userId, groupId, bot, "AV号格式化不正确，请检查后重试。")
                    return
                }
                val bid = av2bv(avId)
                MsgSendUtils.sendAll(userId, groupId, bot, bid)
                log.info { "AVID to BVID - AVID: $avId BVID: $bid" }
            }
        } catch (e: Exception) {
            e.message?.let { MsgSendUtils.sendAll(userId, groupId, bot, "转换异常（${it}）") }
        }
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            action(event.userId, event.groupId, bot, msg)
        }
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        val msg = event.message
        if (msg.matches(regex)) {
            action(event.userId, 0L, bot, msg)
        }
        return MESSAGE_IGNORE
    }

}