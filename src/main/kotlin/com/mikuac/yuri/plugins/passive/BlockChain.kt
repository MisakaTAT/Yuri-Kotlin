package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.dto.BlockChainDto
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.RequestUtils
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.regex.Matcher

@Component
class BlockChain : BotPlugin() {

    private fun request(symbol: String): BlockChainDto {
        val api = "https://api.huobi.pro/market/history/kline?period=1day&size=1&symbol=${symbol}usdt"
        val data = RequestUtils.get(api) ?: throw YuriException("Huobi API 请求失败")
        return Gson().fromJson(data.string(), BlockChainDto::class.java)
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

    // @MessageHandler(cmd = RegexCMD.BLOCK_CHAIN)
    fun handlerBlockChain(bot: Bot, event: WholeMessageEvent, matcher: Matcher) {
        try {
            bot.sendMsg(event, buildMsg(matcher), false)
        } catch (e: YuriException) {
            bot.sendMsg(event, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}