package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.HuobiDTO
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.regex.Matcher

@Shiro
@Component
class Huobi {

    private val cfg = Config.plugins.huobi

    private fun request(symbol: String): HuobiDTO {
        val api = "https://api.huobi.pro/market/history/kline?period=1day&size=1&symbol=${symbol}usdt"
        return NetUtils.get(api, cfg.proxy).use { resp ->
            val data = Gson().fromJson(resp.body?.string(), HuobiDTO::class.java)
            if ("ok" != data.status) throw YuriException("数据获取失败")
            data
        }
    }

    private fun buildMsg(matcher: Matcher): String {
        val symbol = matcher.group(1) ?: throw YuriException("币种获取失败，请检查命令格式。")
        val detail = request(symbol.lowercase())
        val data = detail.data[0]
        val updateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(detail.ts.toLong())
        val range = (data.close - data.open) / data.open
        val toDouble = BigDecimal(range * 100).setScale(4, RoundingMode.HALF_UP).toDouble()
        val upDown = if (range > 0) "+$toDouble%" else "-$toDouble%"
        return """
            币种: ${symbol.uppercase()}/USDT
            最新价格: ${data.close} (${upDown})
            开盘价格: ${data.open}
            日最高价: ${data.high}
            日最低价: ${data.low}
            日交易量: ${data.amount}
            更新时间: $updateTime
        """.trimIndent()
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.Huobi)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            bot.sendMsg(event, buildMsg(matcher), false)
        }
    }

}