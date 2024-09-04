package com.mikuac.yuri.plugins.initiative

import com.google.gson.JsonParser
import com.mikuac.shiro.annotation.GroupMessageHandler
import com.mikuac.shiro.annotation.MessageHandlerFilter
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.yuri.annotation.Slf4j
import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.entity.SteamPlayerStatusEntity
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.global.Global
import com.mikuac.yuri.repository.SteamPlayerStatusRepository
import com.mikuac.yuri.utils.NetUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.regex.Matcher


@Slf4j
@Shiro
@Component
class SteamPlayerStatus {

    @Autowired
    private lateinit var repository: SteamPlayerStatusRepository

    private val cfg = Config.plugins.steamPlayerStatus

    private val cache = HashMap<String, String>()

    private val gamingTime = HashMap<String, LocalDateTime>()

    private val gameNameCache = HashMap<String, String>()

    @Autowired
    private lateinit var global: Global

    private fun getGameName(gameId: String, originalName: String?): String {
        if (gameNameCache.containsKey(gameId)) {
            return gameNameCache[gameId] ?: originalName ?: ""
        }
        val url = "https://store.steampowered.com/api/appdetails?appids=$gameId&cc=cn"
        try {
            val headers = HashMap<String, String>()
            headers["Accept-Language"] = "zh-CN,zh;q=0.9,en;q=0.8"

            val response = NetUtils.get(url, headers, true)
            val jsonObj = JsonParser.parseString(response.body?.string())
            val gameData = jsonObj?.asJsonObject?.get(gameId)?.asJsonObject?.get("data")?.asJsonObject
            val name = gameData?.get("name")?.asString ?: return originalName ?: ""
            if (name != originalName) {
                gameNameCache[gameId] = name
            }
            return name
        } catch (e: Exception) {
            log.error("获取游戏名称失败：$e")
            return originalName ?: ""
        }
    }

    private fun request(groupId: String, player: SteamPlayerStatusEntity): String {
        val playerId = player.steamId;
        val flag = groupId + playerId
        val domain = "https://api.steampowered.com"
        val api = "${domain}/ISteamUser/GetPlayerSummaries/v0002/?key=${cfg.apiKey}&steamids=${playerId}"
        NetUtils.get(api, true).use { resp ->
            val jsonObj = JsonParser.parseString(resp.body?.string())
            val players = jsonObj?.asJsonObject?.get("response")?.asJsonObject?.get("players")?.asJsonArray?.get(0)
            val personaName = "${player.nickname} [Steam]: ${players?.asJsonObject?.get("personaname")?.asString}"
            val gameExtraInfo = players?.asJsonObject?.get("gameextrainfo")?.asString
            val gameId = players?.asJsonObject?.get("gameid")?.asString
            val gameName = if (gameId != null) getGameName(gameId, gameExtraInfo) else gameExtraInfo

            when {
                // 如果发现开始玩了而之前未玩
                gameName != null && cache[flag] == null -> {
                    cache[flag] = gameName
                    gamingTime[flag] = LocalDateTime.now()
                    return " $personaName 正在游玩 ${cache[flag]}"
                }
                // 如果发现开始玩了而之前也在玩
                gameName != null && cache[flag] != null -> {
                    // 如果发现玩的是新游戏
                    if (gameName != cache[flag]) {
                        cache[flag] = gameName
                        gamingTime[flag] = LocalDateTime.now()
                        return " $personaName 正在游玩新游戏 ${cache[flag]}"
                    }
                }
                // 之前在玩，现在没玩
                gameName == null && cache[flag] != null -> {
                    val time = Duration.between(gamingTime[flag], LocalDateTime.now())
                    val msg = " $personaName 停止游玩 ${cache[flag]} 本次游戏时长 ${time.toMinutes()} 分钟"
                    cache.remove(flag)
                    gamingTime.remove(flag)
                    return msg
                }
            }
        }
        return ""
    }

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = Regex.STEAM_PLAYER_STATUS)
    fun handler(event: GroupMessageEvent, bot: Bot, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            val action = matcher.group("action")?.trim() ?: ""
            val steamId = matcher.group("steamId")?.trim() ?: ""
            when (action) {
                "bd" -> {
                    if (steamId.isBlank()) throw YuriException("请输入有效的 Steam64 ID")
                    val isBinned = repository.findByGroupIdAndSteamId(event.groupId, steamId)
                    if (isBinned.isPresent) throw YuriException("该 Steam64 ID 已被他人绑定")
                    val record = repository.findByUserIdAndGroupIdAndSteamId(event.userId, event.groupId, steamId)
                    if (record.isPresent) {
                        record.get().steamId = steamId
                        record.get().nickname = event.sender.nickname ?: event.userId.toString()
                        repository.save(record.get())
                    }
                    if (record.isEmpty) {
                        val data = SteamPlayerStatusEntity(
                            0,
                            event.userId,
                            event.groupId,
                            steamId,
                            event.sender.nickname ?: event.userId.toString()
                        )
                        repository.save(data)
                    }
                    bot.sendGroupMsg(event.groupId, MsgUtils.builder().at(event.userId).text("绑定成功").build(), false)
                }

                "ubd" -> {
                    repository.deleteByUserIdAndGroupId(event.userId, event.groupId)
                    bot.sendGroupMsg(event.groupId, MsgUtils.builder().at(event.userId).text("解绑成功").build(), false)
                }

                "subs" -> {
                    val msg = MsgUtils.builder()
                    val subs = repository.findByGroupId(event.groupId)
                    if (subs.isEmpty()) throw YuriException("暂无 Steam 订阅")
                    subs.forEach {
                        msg.text("[${it.nickname}]${it.steamId}\n")
                    }
                    bot.sendGroupMsg(event.groupId, msg.build(), false)
                }
            }
        }
    }


    @Scheduled(cron = "0 0/1 * * * ?", zone = "Asia/Shanghai")
    fun scheduled() {
        val bot = global.bot()
        log.info("开始处理 Steam 玩家状态订阅")
        getSteamIdsByGroupId().forEach { group ->
            log.info("开始处理群：$group，订阅数：${group.value.size}")
            group.value.forEach { player ->
                val msg = request(group.toString(), player)
                if (msg.isNotBlank()) bot.sendGroupMsg(group.key.toLong(), msg, false)
            }
        }
        log.info("Steam 玩家状态订阅处理完毕")
    }

    private fun getSteamIdsByGroupId(): Map<Long, MutableList<SteamPlayerStatusEntity>> {
        return repository.findAll()
            .groupBy { it.groupId }
            .mapValues { (_, entities) -> entities.toMutableList() }
        //.mapValues { (_, entities) -> entities.map { it.steamId }.toMutableList() }
    }

}