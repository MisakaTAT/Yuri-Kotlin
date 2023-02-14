package com.mikuac.yuri.utils

import com.alibaba.fastjson2.annotation.JSONField
import com.alibaba.fastjson2.to
import com.mikuac.yuri.config.Config

object TelegramUtils {

    @JvmStatic
    fun getFile(fileId: String): String? {
        val baseURL = "https://api.telegram.org"
        val botToken = Config.plugins.telegram.botToken
        val api = "${baseURL}/bot${botToken}/getFile?file_id=${fileId}"
        val resp = NetUtils.get(api, true)
        val data = resp.body?.string().to<Result>()
        resp.close()
        if (data.ok) {
            return "${baseURL}/file/bot${botToken}/${data.result.filePath}"
        }
        return null
    }

    data class Result(
        val ok: Boolean, val result: Child
    ) {
        data class Child(
            @JSONField(name = "file_id")
            val fileId: String,
            @JSONField(name = "file_path")
            val filePath: String,
            @JSONField(name = "file_size")
            val fileSize: Int,
            @JSONField(name = "file_unique_id")
            val fileUniqueId: String
        )
    }

}





