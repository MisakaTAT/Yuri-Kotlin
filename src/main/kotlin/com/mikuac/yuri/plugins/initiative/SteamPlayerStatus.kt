package com.mikuac.yuri.plugins.initiative

import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.global.Global
import com.mikuac.yuri.utils.NetUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Slf4j
@Shiro
@Component
class SteamPlayerStatus {

    private val cfg = Config.plugins.steamPlayerStatus

    private val cache = HashMap<String, String>()

    private val gamingTime = HashMap<String, LocalDateTime>()

    @Autowired
    private lateinit var global: Global

    private fun request(groupId: String, playerId: String): String {
        val flag = groupId + playerId
        val domain = "https://api.steampowered.com"
        val api = "${domain}/ISteamUser/GetPlayerSummaries/v0002/?key=${cfg.apiKey}&steamids=${playerId}"
        NetUtils.get(api).use { resp ->
            val jsonObj = JsonParser.parseString(resp.body?.string())
            val players = jsonObj.asJsonObject["response"].asJsonObject["players"].asJsonArray[0]
            val personaName = players.asJsonObject["personaname"].asString
            val gameName = players.asJsonObject["gameextrainfo"]
            when {
                // 如果发现开始玩了而之前未玩
                gameName != null && cache[flag] == null -> {
                    cache[flag] = gameName.asString
                    gamingTime[flag] = LocalDateTime.now()
                    return "[Steam] $personaName 正在游玩 ${cache[flag]}"
                }
                // 如果发现开始玩了而之前也在玩
                gameName != null && cache[flag] != null -> {
                    // 如果发现玩的是新游戏
                    if (gameName.asString != cache[flag]) {
                        cache[flag] = gameName.asString
                        gamingTime[flag] = LocalDateTime.now()
                        return "[Steam] $personaName 正在游玩新游戏 ${cache[flag]}"
                    }
                }
                // 之前在玩，现在没玩
                gameName == null && cache[flag] != null -> {
                    val time = Duration.between(gamingTime[flag], LocalDateTime.now())
                    val msg = "[Steam] $personaName 停止游玩 ${cache[flag]} 本次游戏时长 ${time.toMinutes()} 分钟"
                    cache.remove(flag)
                    gamingTime.remove(flag)
                    return msg
                }
            }
        }
        return ""
    }

    @Scheduled(cron = "0 0/1 * * * ?", zone = "Asia/Shanghai")
    fun handler() {
        val bot = global.bot()
        log.info("开始处理 Steam 玩家状态订阅")
        cfg.subscriber.forEach { group ->
            log.info("开始处理群：$group，订阅数：${group.value.size}")
            group.value.forEach { playerId ->
                val msg = request(group.toString(), playerId)
                if (msg.isNotBlank()) bot.sendGroupMsg(group.key.toLong(), msg, false)
            }
        }
        log.info("Steam 玩家状态订阅处理完毕")
    }

}