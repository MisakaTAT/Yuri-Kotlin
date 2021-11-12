package com.mikuac.yuri.utils

import com.mikuac.yuri.config.ReadConfig
import com.tencentcloudapi.common.Credential
import com.tencentcloudapi.common.profile.ClientProfile
import com.tencentcloudapi.common.profile.HttpProfile
import com.tencentcloudapi.nlp.v20190408.NlpClient
import com.tencentcloudapi.nlp.v20190408.models.ChatBotRequest
import com.tencentcloudapi.tbp.v20190627.TbpClient
import com.tencentcloudapi.tbp.v20190627.models.TextProcessRequest

object TencentUtils {

    fun tbp(query: String): String {
        val config = ReadConfig.config.plugin.tencentTBP
        val cred = Credential(config.secretId, config.secretKey)
        val httpProfile = HttpProfile()
        httpProfile.endpoint = "tbp.tencentcloudapi.com"
        val clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        val client = TbpClient(cred, "", clientProfile)
        val req = TextProcessRequest()
        req.botId = config.botId
        req.botEnv = config.botEnv
        req.terminalId = config.terminalId
        req.inputText = query
        val resp = client.TextProcess(req)
        return resp.responseMessage.groupList[0].content
    }

    fun nlp(query: String): String {
        val config = ReadConfig.config.plugin.tencentNLP
        val cred = Credential(config.secretId, config.secretKey)
        val httpProfile = HttpProfile()
        httpProfile.endpoint = "nlp.tencentcloudapi.com"
        val clientProfile = ClientProfile()
        clientProfile.httpProfile = httpProfile
        val client = NlpClient(cred, "ap-guangzhou", clientProfile)
        val req = ChatBotRequest()
        req.query = query
        return client.ChatBot(req).reply
    }

}