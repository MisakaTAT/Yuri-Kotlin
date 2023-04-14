@file:Suppress("SpellCheckingInspection")

package com.mikuac.yuri.plugins.initiative

import com.google.gson.Gson
import com.google.gson.JsonObject
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
import java.security.SecureRandom
import javax.script.ScriptEngineManager

@Shiro
@Component
class DouYinParse {

    private val agent =
        "Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Mobile Safari/537.36 Edg/87.0.664.66"

    private fun signURL(id: String): String {
        val url = "https://www.douyin.com/aweme/v1/web/aweme/detail/?aweme_id=$id"
        val query = URL(url).query
        val engine = ScriptEngineManager().getEngineByName("js")
        val script = this::class.java.getResource("/douyin-sign.js")!!.readText()
        val sign = engine.eval("$script; sign('$query', '$agent')") as String
        return "$url&X-Bogus=$sign"
    }

    fun msToken(length: Int): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val secureRandom = SecureRandom()
        val randomBytes = ByteArray(length)
        secureRandom.nextBytes(randomBytes)
        return randomBytes.map { byte -> characters[(byte.toInt() and 0xFF) % characters.length] }.joinToString("")
    }

    private fun createCookie(): String {
        val v1 = msToken(107)
        val v2 = "324fb4ea4a89c0c05827e18a1ed9cf9bf8a17f7705fcc793fec935b637867e2a5a9b8168c885554d029919117a18ba69"
        val v3 =
            "eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWNsaWVudC1jc3IiOiItLS0tLUJFR0lOIENFUlRJRklDQVRFIFJFUVVFU1QtLS0tLVxyXG5NSUlCRFRDQnRRSUJBREFuTVFzd0NRWURWUVFHRXdKRFRqRVlNQllHQTFVRUF3d1BZbVJmZEdsamEyVjBYMmQxXHJcbllYSmtNRmt3RXdZSEtvWkl6ajBDQVFZSUtvWkl6ajBEQVFjRFFnQUVKUDZzbjNLRlFBNUROSEcyK2F4bXAwNG5cclxud1hBSTZDU1IyZW1sVUE5QTZ4aGQzbVlPUlI4NVRLZ2tXd1FJSmp3Nyszdnc0Z2NNRG5iOTRoS3MvSjFJc3FBc1xyXG5NQ29HQ1NxR1NJYjNEUUVKRGpFZE1Cc3dHUVlEVlIwUkJCSXdFSUlPZDNkM0xtUnZkWGxwYmk1amIyMHdDZ1lJXHJcbktvWkl6ajBFQXdJRFJ3QXdSQUlnVmJkWTI0c0RYS0c0S2h3WlBmOHpxVDRBU0ROamNUb2FFRi9MQnd2QS8xSUNcclxuSURiVmZCUk1PQVB5cWJkcytld1QwSDZqdDg1czZZTVNVZEo5Z2dmOWlmeTBcclxuLS0tLS1FTkQgQ0VSVElGSUNBVEUgUkVRVUVTVC0tLS0tXHJcbiJ9"
        val v4 = ttwid()
        return "msToken=${v1};odin_tt=${v2};bd_ticket_guard_client_data=${v3};ttwid=${v4};"
    }

    private fun ttwid(): String {
        val migrateInfo = JsonObject()
        migrateInfo.addProperty("ticket", "")
        migrateInfo.addProperty("source", "node")
        val data = JsonObject()
        data.addProperty("region", "cn")
        data.addProperty("aid", 1768)
        data.addProperty("needFid", false)
        data.addProperty("service", "www.ixigua.com")
        data.addProperty("cbUrlProtocol", "https")
        data.addProperty("union", true)
        data.add("migrate_info", migrateInfo)
        return NetUtils.post("https://ttwid.bytedance.com/ttwid/union/register/", data.toString()).use { resp ->
            resp.headers["Set-Cookie"]?.let { RegexUtils.group("ttwid", it, "ttwid=(?<ttwid>[^;]+)") } ?: ""
        }
    }

    private fun request(msg: String): DouYinParseDTO.Detail {
        val shortURL = RegexUtils.group("url", msg, Regex.DOU_YIN_SHORT_URL).trim()
        if (shortURL.isBlank()) throw YuriException("抖音短链接提取失败")
        val videoId = NetUtils.get(shortURL).use { resp ->
            val id = RegexUtils.group("id", resp.request.url.toString(), Regex.DOU_YIN_REAL_URL_ID)
            if (id.isBlank()) throw YuriException("抖音视频ID提取失败")
            id
        }

        val headers = HashMap<String, String>()
        headers["User-Agent"] = agent
        headers["Cookie"] = createCookie()
        headers["Referer"] = "https://www.douyin.com/"
        return NetUtils.get(signURL(videoId), headers).use { resp ->
            Gson().fromJson(resp.body?.string(), DouYinParseDTO::class.java) ?: throw YuriException("抖音链接解析失败")
        }.detail
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