package com.mikuac.yuri.plugins.aop

import com.google.gson.Gson
import com.mikuac.shiro.common.utils.MsgUtils
import com.mikuac.shiro.core.Bot
import com.mikuac.shiro.core.BotPlugin
import com.mikuac.shiro.dto.event.message.GroupMessageEvent
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent
import com.mikuac.yuri.common.utils.*
import com.mikuac.yuri.dto.GithubRepoDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GithubRepo : BotPlugin() {

    private val regex = Regex("^(?i)github\\s(-p)?+\\s?(.*)\$")

    @Autowired
    private lateinit var checkUtils: CheckUtils

    private fun getRepoInfo(repoName: String): GithubRepoDto {
        val api = "https://api.github.com/search/repositories?q=${repoName}"
        val result = RequestUtils.get(api)
        val json = Gson().fromJson(result, GithubRepoDto::class.java)
        if (json.totalCount <= 0) throw Exception("未找到相关仓库")
        return json
    }

    private fun sendMsg(msg: String, userId: Long, groupId: Long, bot: Bot) {
        if (!msg.matches(regex)) return
        if (!checkUtils.basicCheck(this.javaClass.simpleName, userId, groupId, bot)) return
        try {
            val searchName = RegexUtils.group(regex, 2, msg)
            val data = getRepoInfo(searchName).items[0]
            val buildMsg: String = if (RegexUtils.group(regex, 1, msg) == "-p") {
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
            MsgSendUtils.atSend(userId, groupId, bot, "GitHub仓库查询失败 ${e.message}")
            LogUtils.debug("${DateUtils.getTime()} ${this.javaClass.simpleName} Exception")
            LogUtils.debug(e.stackTraceToString())
        }
    }

    override fun onPrivateMessage(bot: Bot, event: PrivateMessageEvent): Int {
        sendMsg(event.message, event.userId, 0L, bot)
        return MESSAGE_IGNORE
    }

    override fun onGroupMessage(bot: Bot, event: GroupMessageEvent): Int {
        sendMsg(event.message, event.userId, event.groupId, bot)
        return MESSAGE_IGNORE
    }

}