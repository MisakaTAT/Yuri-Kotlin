package com.mikuac.yuri.plugins

import com.mikuac.shiro.core.BotPlugin
import com.mikuac.yuri.config.ReadConfig
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.tbp.v20190627.TbpClient
import com.tencentcloudapi.tbp.v20190627.models.TextProcessRequest
import com.tencentcloudapi.tbp.v20190627.models.TextProcessResponse
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class TencentTBP : BotPlugin() {

    @PostConstruct
    private fun request() {
        val config = ReadConfig.config.plugin.tencentNLP
        val cred = Credential(config.secretId, config.secretKey)
        val httpProfile = HttpProfile()
        httpProfile.endpoint = "tbp.tencentcloudapi.com"
        val clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        val client = TbpClient(cred, "", clientProfile)
        val req = TextProcessRequest()
        req.botId = "2809fe47-966d-4301-a533-a1cd31d0da84"
        req.botEnv = "dev"
        req.terminalId = "yuri-kotlin"
        req.inputText = "这是什么群?"
        val resp = client.TextProcess(req)
        println(resp.responseMessage.groupList[0].content)
    }

}