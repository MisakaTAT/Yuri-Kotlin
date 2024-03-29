package com.mikuac.yuri.plugins.initiative

import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.global.Global
import com.mikuac.yuri.utils.NetUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Shiro
@Component
class SteamPlayerStatus {

    private val cfg = Config.plugins.steamPlayerStatus

    private val cache = HashMap<String, String>()

    @Autowired
    private lateinit var global: Global

    private fun request(playerId: String): String {
        val domain = "https://api.steampowered.com"
        val api = "${domain}/ISteamUser/GetPlayerSummaries/v0002/?key=${cfg.apiKey}&steamids=${playerId}"
        NetUtils.get(api).use { resp ->
            val jsonObj = JsonParser.parseString(resp.body?.string())
            val players = jsonObj.asJsonObject["response"].asJsonObject["players"].asJsonArray[0]
            val personaName = players.asJsonObject["personaname"].asString
            val gameName = players.asJsonObject["gameextrainfo"]
            when {
                // 如果发现开始玩了而之前未玩
                gameName != null && cache[playerId] == null -> {
                    cache[playerId] = gameName.asString
                    return "[Steam] $personaName 正在游玩 ${cache[playerId]}"
                }
                // 如果发现开始玩了而之前也在玩
                gameName != null && cache[playerId] != null -> {
                    // 如果发现玩的是新游戏
                    if (gameName.asString != cache[playerId]) {
                        cache[playerId] = gameName.asString
                        return "[Steam] $personaName 正在游玩新游戏 ${cache[playerId]}"
                    }
                }
                // 之前在玩，现在没玩
                // TODO: 计算玩了多久
                gameName == null && cache[playerId] != null -> {
                    val msg = "[Steam] $personaName 停止游玩 ${cache[playerId]}"
                    cache.remove(playerId)
                    return msg
                }
            }
        }
        return ""
    }

    @Scheduled(cron = "0 0/1 * * * ?", zone = "Asia/Shanghai")
    fun handler() {
        val bot = global.bot()
        cfg.subscriber.forEach { group ->
            group.value.forEach { players ->
                val msg = request(players)
                if (msg.isNotBlank()) bot.sendGroupMsg(group.key.toLong(), msg, false)
            }
        }
    }

}