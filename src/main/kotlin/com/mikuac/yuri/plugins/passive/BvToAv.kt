package com.mikuac.yuri.plugins.passive

import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
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

    private fun buildMsg(msg: String): String? {
        try {
            val bvId = RegexUtils.group(RegexCMD.BV_AV_CONVERT.toRegex(), 1, msg)
            val avId = RegexUtils.group(RegexCMD.BV_AV_CONVERT.toRegex(), 2, msg)
            if (bvId.isNotEmpty()) return bv2av(bvId)
            if (avId.isNotEmpty()) return av2bv(avId)
        } catch (e: Exception) {
            throw YuriException("转换失败")
        }
        return null
    }

    @MessageHandler(cmd = RegexCMD.BV_AV_CONVERT)
    fun handler(bot: Bot, event: WholeMessageEvent) {
        try {
            val msg = buildMsg(event.message) ?: return
            bot.sendMsg(event, msg, false)
        } catch (e: YuriException) {
            bot.sendMsg(event, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}