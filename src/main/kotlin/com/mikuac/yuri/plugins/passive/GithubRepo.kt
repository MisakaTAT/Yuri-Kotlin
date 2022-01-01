package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.MessageHandler
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.WholeMessageEvent
import com.mikuac.yuri.dto.GithubRepoDto
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.RegexUtils
import com.mikuac.yuri.utils.RequestUtils
import org.springframework.stereotype.Component

@Component
class GithubRepo : BotPlugin() {

    private fun getRepoInfo(repoName: String): GithubRepoDto {
        val api = "https://api.github.com/search/repositories?q=${repoName}"
        val result = RequestUtils.get(api)
        val json = Gson().fromJson(result, GithubRepoDto::class.java)
        if (json.totalCount <= 0) throw YuriException("未找到名为 $repoName 的仓库")
        return json
    }

    private fun buildMsg(msg: String): String {
        val searchName = RegexUtils.group(RegexCMD.GITHUB_REPO.toRegex(), 1, msg)
        val data = getRepoInfo(searchName).items[0]
        return MsgUtils.builder()
            .text("RepoName: ${data.fullName}")
            .text("\nDefaultBranch: ${data.defaultBranch}")
            .text("\nLanguage: ${data.language}")
            .text("\nStars/Forks: ${data.stars}/${data.forks}")
            .text("\nLicense: ${data.license?.spdxId}")
            .text("\n${data.description}")
            .text("\n${data.htmlUrl}")
            .img("https://opengraph.githubassets.com/0/${data.fullName}")
            .build()
    }

    @MessageHandler(cmd = RegexCMD.GITHUB_REPO)
    fun githubRepoHandler(bot: Bot, event: WholeMessageEvent) {
        try {
            val msg = buildMsg(event.message)
            bot.sendMsg(event, msg, false)
        } catch (e: YuriException) {
            bot.sendMsg(event, e.message, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}