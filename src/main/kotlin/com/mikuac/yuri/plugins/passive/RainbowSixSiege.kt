package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.dto.RainbowSixSiegeDTO
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.FormatUtils
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class RainbowSixSiege {

    private fun request(username: String): RainbowSixSiegeDTO {
        if (username.isEmpty()) throw YuriException("用户名不合法，请检查输入是否正确。")
        val data: RainbowSixSiegeDTO
        val resp = NetUtils.get(
            "https://www.r6s.cn/Stats?username=${username}",
            mapOf(Pair("referer", "no-referer"))
        )
        data = Gson().fromJson(resp.body?.string(), RainbowSixSiegeDTO::class.java)
        resp.close()
        if (data.status != 200) throw YuriException("服务器可能爆炸惹，请稍后重试～")
        return data
    }

    private fun ratioComputed(a: Int, b: Int): String {
        return FormatUtils.getNoMoreThanTwoDigits(a.toDouble() / b.toDouble())
    }

    private fun buildMsg(data: RainbowSixSiegeDTO): String {
        val basicStat = data.basicStat.filter { "apac" == it.region }[0]
        val statGeneral = data.statGeneral[0]
        val ranked = data.statCR.filter { "ranked" == it.model }[0]
        val casual = data.statCR.filter { "casual" == it.model }[0]
        val secureAreaPVP = data.statBHS.filter { "secureareapvp" == it.model }[0]
        val plantBombPVP = data.statBHS.filter { "plantbombpvp" == it.model }[0]
        val rescueHostagePVP = data.statBHS.filter { "rescuehostagepvp" == it.model }[0]

        val generalWinLoseRatio = ratioComputed(statGeneral.won, statGeneral.lost)
        val generalBattleDamageRatio = ratioComputed(statGeneral.kills, statGeneral.deaths)
        val generalHeatShot = ratioComputed(statGeneral.headshot, statGeneral.kills).toDouble() * 100
        val rankedWinLoseRatio = ratioComputed(ranked.won, ranked.lost)
        val rankedBattleDamageRatio = ratioComputed(ranked.kills, ranked.deaths)
        val casualWinLoseRatio = ratioComputed(casual.won, casual.lost)
        val casualBattleDamageRatio = ratioComputed(casual.kills, casual.deaths)

        return MsgUtils.builder()
            .text("[基本信息]")
            .text("\n查询状态码: ${data.status} 区服: ${basicStat.region}")
            .text("\n用户名: ${data.username} 游戏等级: ${basicStat.level}")
            .text("\n数据更新时间: ${basicStat.updatedAt}")

            .text("\n\n[当前赛季排位信息]")
            .text("\n胜场: ${basicStat.wins} 败场: ${basicStat.losses} 弃赛: ${basicStat.abandons}")
            .text("\n当前MMR: ${basicStat.mmr} 最高MMR: ${basicStat.maxMmr} 赛季: ${basicStat.season}")
            .text("\n当前段位: ${basicStat.rank} 最高段位: ${basicStat.maxRank} 平台: ${basicStat.platform}")

            .text("\n\n[综合数据]")
            .text("\n总胜场: ${statGeneral.won} 总败场: ${statGeneral.lost} 胜负比: ${generalWinLoseRatio}%")
            .text("\n总击杀: ${statGeneral.kills} 总死亡: ${statGeneral.deaths} 战损比: ${generalBattleDamageRatio}%")
            .text("\n总开火次数: ${statGeneral.bulletsFired} 总命中次数: ${statGeneral.bulletsHit}")
            .text("\n助攻: ${statGeneral.killAssists} 近战: ${statGeneral.meleeKills} 救助: ${statGeneral.revives} 爆头: ${statGeneral.headshot}")
            .text("\n穿透击杀: ${statGeneral.penetrationKills} 爆头率: ${generalHeatShot}%")

            .text("\n\n[排名战数据]")
            .text("\n胜场: ${ranked.won} 败场: ${ranked.lost} 胜负比: ${rankedWinLoseRatio}% 游戏场次: ${ranked.played}")
            .text("\n击杀: ${ranked.kills} 阵亡: ${ranked.deaths} 战损比: ${rankedBattleDamageRatio}%")

            .text("\n\n[常规战数据]")
            .text("\n胜场: ${casual.won} 败场: ${casual.lost} 胜负比: ${casualWinLoseRatio}% 游戏场次: ${casual.played}")
            .text("\n击杀: ${casual.kills} 阵亡: ${casual.deaths} 战损比: ${casualBattleDamageRatio}%")

            .text("\n\n[模式数据-肃清威胁]")
            .text("\n胜场: ${secureAreaPVP.won} 败场: ${secureAreaPVP.lost} 总场次: ${secureAreaPVP.played} 最高得分: ${secureAreaPVP.bestScore}")

            .text("\n\n[模式数据-炸弹模式]")
            .text("\n胜场: ${plantBombPVP.won} 败场: ${plantBombPVP.lost} 总场次: ${plantBombPVP.played} 最高得分: ${plantBombPVP.bestScore}")

            .text("\n\n[模式数据-人质模式]")
            .text("\n胜场: ${rescueHostagePVP.won} 败场: ${rescueHostagePVP.lost} 总场次: ${rescueHostagePVP.played} 最高得分: ${rescueHostagePVP.bestScore}")
            .build()
    }

    @AnyMessageHandler(cmd = RegexCMD.R6S)
    fun r6sHandler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        try {
            val username = matcher.group(1) ?: YuriException("用户名获取失败")
            val data = request(username.toString().trim())
            bot.sendMsg(event, buildMsg(data), false)
        } catch (e: IndexOutOfBoundsException) {
            SendUtils.reply(event, bot, "未查询到此ID游戏数据")
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}