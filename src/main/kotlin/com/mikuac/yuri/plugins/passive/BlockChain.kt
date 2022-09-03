package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.Shiro
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.bean.dto.BlockChainDto
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RequestUtils
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.regex.Matcher

@Shiro
@Component
class BlockChain {

    private fun request(symbol: String): BlockChainDto {
        val data: BlockChainDto
        try {
            val api = "https://api.huobi.pro/market/history/kline?period=1day&size=1&symbol=${symbol}usdt"
            val resp = RequestUtils.get(api)
            data = Gson().fromJson(resp.body?.string(), BlockChainDto::class.java)
            resp.close()
            if ("ok" != data.status) throw YuriException("数据获取失败")
        } catch (e: Exception) {
            throw YuriException("火币数据获取异常：${e.message}")
        }
        return data
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
    fun blockChainHandler(bot: Bot, event: WholeMessageEvent, matcher: Matcher) {
        try {
            bot.sendMsg(event, buildMsg(matcher), false)
        } catch (e: YuriException) {
            e.message?.let { MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, it) }
        } catch (e: Exception) {
            MsgSendUtils.replySend(event.messageId, event.userId, event.groupId, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}