package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.OneBotMedia
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.dto.GithubRepoDTO
import com.mikuac.yuri.enums.Regex
import com.mikuac.yuri.exception.ExceptionHandler
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.ImageUtils
import com.mikuac.yuri.utils.NetUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class GithubRepo {

    private val cfg = Config.plugins.githubRepo

    private fun request(repoName: String): GithubRepoDTO {
        return NetUtils.get("https://api.github.com/search/repositories?q=${repoName}", cfg.proxy).use { resp ->
            val data = Gson().fromJson(resp.body?.string(), GithubRepoDTO::class.java)
            if (data.totalCount <= 0) throw YuriException("未找到名为 $repoName 的仓库")
            data
        }
    }

    private fun buildMsg(matcher: Matcher): String {
        val repoName = matcher.group("repo").trim()
        if (repoName.isBlank()) throw YuriException("请按格式输入正确的仓库名")
        val data = request(repoName).items[0]
        val img = ImageUtils.formatPNG("https://opengraph.githubassets.com/0/${data.fullName}", cfg.proxy)
        return MsgUtils.builder()
            .text("RepoName: ${data.fullName}")
            .text("\nDefaultBranch: ${data.defaultBranch}")
            .text("\nLanguage: ${data.language}")
            .text("\nStars/Forks: ${data.stars}/${data.forks}")
            .text("\nLicense: ${data.license?.spdxId}")
            .text("\n${data.description}")
            .text("\n${data.htmlUrl}")
            .img(
                OneBotMedia.builder()
                    .file(img)
                    .cache(false)
            )
            .build()
    }

    @AnyMessageHandler(cmd = Regex.GITHUB_REPO)
    fun handler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        ExceptionHandler.with(bot, event) {
            bot.sendMsg(event, buildMsg(matcher), false)
        }
    }

}