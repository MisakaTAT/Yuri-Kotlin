package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher
import kotlin.math.pow

@Shiro
@Component
class BvToAv {

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

    private fun buildMsg(matcher: Matcher): String {
        val bvId = matcher.group(1)
        val avId = matcher.group(2)
        if (bvId != null && bvId.isNotEmpty()) return bv2av(bvId)
        if (avId != null && avId.isNotEmpty()) return av2bv(avId)
        return "转换失败"
    }

    @MessageHandler(cmd = RegexCMD.BV_AV_CONVERT)
    fun bvToAvHandler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        try {
            bot.sendMsg(event, buildMsg(matcher), false)
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "格式非法")
            e.printStackTrace()
        }
    }

}