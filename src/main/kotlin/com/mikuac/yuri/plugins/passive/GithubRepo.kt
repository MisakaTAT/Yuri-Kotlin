package com.mikuac.yuri.plugins.passive

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mikuac.shiro.annotation.AnyMessageHandler
import com.mikuac.shiro.annotation.common.Shiro
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.common.utils.OneBotMedia
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.dto.event.message.AnyMessageEvent
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.enums.RegexCMD
import com.mikuac.yuri.exception.YuriException
import com.mikuac.yuri.utils.ImageUtils
import com.mikuac.yuri.utils.NetUtils
import com.mikuac.yuri.utils.SendUtils
import org.springframework.stereotype.Component
import java.util.regex.Matcher

@Shiro
@Component
class GithubRepo {

    data class GithubRepo(
        @SerializedName("total_count")
        val totalCount: Int,
        val items: List<Items>
    ) {
        data class Items(
            val description: String,
            val language: String,
            val license: License? = null,
            @SerializedName("full_name")
            val fullName: String,
            @SerializedName("html_url")
            val htmlUrl: String,
            @SerializedName("forks_count")
            val forks: Int,
            @SerializedName("stargazers_count")
            val stars: Int,
            @SerializedName("default_branch")
            val defaultBranch: String,
        ) {
            data class License(
                @SerializedName("spdx_id")
                val spdxId: String? = null
            )
        }
    }

    private fun request(repoName: String): GithubRepo {
        val data: GithubRepo
        val api = "https://api.github.com/search/repositories?q=${repoName}"
        val resp = NetUtils.get(api)
        data = Gson().fromJson(resp.body?.string(), GithubRepo::class.java)
        resp.close()
        if (data.totalCount <= 0) throw YuriException("未找到名为 $repoName 的仓库")
        return data
    }

    private fun buildMsg(matcher: Matcher): String {
        val searchName = matcher.group(1) ?: throw YuriException("请按格式输入正确的仓库名")
        val data = request(searchName).items[0]
        val img = ImageUtils.formatPNG(
            "https://opengraph.githubassets.com/0/${data.fullName}",
            Config.plugins.githubRepo.proxy
        )
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

    @AnyMessageHandler(cmd = RegexCMD.GITHUB_REPO)
    fun githubRepoHandler(bot: Bot, event: AnyMessageEvent, matcher: Matcher) {
        try {
            bot.sendMsg(event, buildMsg(matcher), false)
        } catch (e: YuriException) {
            e.message?.let { SendUtils.reply(event, bot, it) }
        } catch (e: Exception) {
            SendUtils.reply(event, bot, "未知错误：${e.message}")
            e.printStackTrace()
        }
    }

}