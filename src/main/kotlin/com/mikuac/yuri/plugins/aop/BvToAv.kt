package com.mikuac.yuri.plugins.aop

import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.LogUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component
import kotlin.math.pow


@Component
class BvToAv : BotPlugin() {

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

    private fun handler(msgId: Int, msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.BV_TO_AV.value)) return
        buildMsg(msgId, msg, userId, groupId, bot)
    }

    private fun buildMsg(msgId: Int, msg: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            val bvId = RegexUtils.group(RegexEnum.BV_TO_AV.value, 1, msg)
            val avId = RegexUtils.group(RegexEnum.BV_TO_AV.value, 2, msg)
            if (bvId.isNotEmpty()) {
                if (!bvId.matches(Regex("^(?i)BV(.*)"))) {
                    MsgSendUtils.atSend(userId, groupId, bot, "BV号格式化不正确，请检查后重试。")
                    return
                }
                val aid = bv2av(bvId)
                MsgSendUtils.atSend(userId, groupId, bot, aid)
            }
            if (avId.isNotEmpty()) {
                if (!avId.matches(Regex("^(?i)AV(.*)"))) {
                    MsgSendUtils.atSend(userId, groupId, bot, "AV号格式化不正确，请检查后重试。")
                    return
                }
                val bid = av2bv(avId)
                MsgSendUtils.atSend(userId, groupId, bot, bid)
            }
        } catch (e: Exception) {
            MsgSendUtils.errorSend(msgId, userId, groupId, bot, "AV/BV 转换失败", e.message)
            LogUtils.error(e.stackTraceToString())
        }
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        handler(event.messageId, event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        handler(event.messageId, event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

}