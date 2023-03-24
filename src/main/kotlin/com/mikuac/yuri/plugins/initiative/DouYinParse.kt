package com.mikuac.yuri.plugins.initiative

import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.dto.DouYinParseDTO
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.RegexUtils
import org.springframework.stereotype.Component
import java.net.URL
import javax.script.ScriptEngineManager

@Shiro
@Component
class DouYinParse {

    private val headers = object : HashMap<String, String>() {
        init {
            put("Cookie", "s_v_web_id=verify_leytkxgn_kvO5kOmO_SdMs_4t1o_B5ml_BUqtWM1mP6BF;")
        }
    }

    private val agent = """
        Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Mobile Safari/537.36 Edg/87.0.664.66
    """.trimIndent()

    private fun signURL(id: String): String {
        val url = "https://www.douyin.com/aweme/v1/web/aweme/detail/?aweme_id=$id"
        val query = URL(url).query
        val engine = ScriptEngineManager().getEngineByName("js")
        val script = this::class.java.getResource("/douyin-sign.js")!!.readText()
        val sign = engine.eval("$script; sign('$query', '$agent')") as String
        return "$url&X-Bogus=$sign"
    }

    private fun request(msg: String): DouYinParseDTO.Detail {
        val shortURL = RegexUtils.group("url", msg, Regex.DOU_YIN_SHORT_URL)
        if (shortURL.isBlank()) throw YuriException("抖音短链接提取失败")
        val firstResp = NetUtils.get(shortURL)
        val videoID = RegexUtils.group(
            "id",
            firstResp.request.url.toString(),
            Regex.DOU_YIN_REAL_URL_ID,
        )
        firstResp.close()
        if (videoID.isBlank()) throw YuriException("抖音视频ID提取失败")
        val data: DouYinParseDTO
        val resp = NetUtils.get(signURL(videoID), headers)
        data = Gson().fromJson(resp.body?.string(), DouYinParseDTO::class.java) ?: throw YuriException("抖音链接解析失败")
        resp.close()
        return data.detail
    }

    @AnyMessageHandler
    fun handler(bot: Bot, event: AnyMessageEvent) {
        ExceptionHandler.with(bot, event) {
            if (!RegexUtils.check(event.message, Regex.DOU_YIN_SHORT_URL)) return@with
            val data = request(event.message)
            bot.sendMsg(
                event,
                MsgUtils.builder().video(data.video.play.urls[0], data.video.cover.urls[0]).build(),
                false
            )
        }
    }

}