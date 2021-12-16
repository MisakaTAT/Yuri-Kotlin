package com.mikuac.yuri.plugins.aop

import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.dto.GithubRepoDto
import com.mikuac.yuri.enums.RegexEnum
import com.mikuac.yuri.utils.LogUtils
import com.mikuac.yuri.utils.MsgSendUtils
import com.mikuac.yuri.utils.RegexUtils
import com.mikuac.yuri.utils.RequestUtils
import org.springframework.stereotype.Component

@Component
class GithubRepo : BotPlugin() {

    private fun getRepoInfo(repoName: String): GithubRepoDto {
        val api = "https://api.github.com/search/repositories?q=${repoName}"
        val result = RequestUtils.get(api)
        val json = Gson().fromJson(result, GithubRepoDto::class.java)
        if (json.totalCount <= 0) throw RuntimeException("未找到名为 $repoName 的仓库")
        return json
    }

    private fun handler(msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(RegexEnum.GITHUB_REPO.value)) return
        buildMsg(msg, userId, groupId, bot)
    }

    private fun buildMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        try {
            val searchName = RegexUtils.group(RegexEnum.GITHUB_REPO.value, 2, msg)
            val data = getRepoInfo(searchName).items[0]
            val buildMsg: String = if (RegexUtils.group(RegexEnum.GITHUB_REPO.value, 1, msg) == "-p") {
                MsgUtils.builder().img("https://opengraph.githubassets.com/0/${data.fullName}").build()
            } else {
                MsgUtils.builder()
                    .text("RepoName: ${data.fullName}")
                    .text("\nDefaultBranch: ${data.defaultBranch}")
                    .text("\nLanguage: ${data.language}")
                    .text("\nStars/Forks: ${data.stars}/${data.forks}")
                    .text("\nLicense: ${data.license?.spdxId}")
                    .text("\n${data.description}")
                    .text("\n${data.htmlUrl}")
                    .build()
            }
            MsgSendUtils.send(userId, groupId, bot, buildMsg)
        } catch (e: Exception) {
            MsgSendUtils.errorSend(
                userId, groupId, bot,
                "查询失败啦，有没有一种可能，我只是说一种猜测，这个仓库不存在呢？TAT", e.message
            )
            LogUtils.error(e.stackTraceToString())
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        handler(event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        handler(event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}