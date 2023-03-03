package com.mikuac.yuri.utils

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mikuac.yuri.config.Config

object TelegramUtils {

    @JvmStatic
    fun getFile(fileId: String): String? {
        val baseURL = "https://api.telegram.org"
        val botToken = Config.plugins.telegram.botToken
        val api = "${baseURL}/bot${botToken}/getFile?file_id=${fileId}"
        val resp = NetUtils.get(api, true)
        val data = Gson().fromJson(resp.body?.string(), Result::class.java)
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
            @SerializedName("file_id")
            val fileId: String,
            @SerializedName("file_path")
            val filePath: String,
            @SerializedName("file_size")
            val fileSize: Int,
            @SerializedName("file_unique_id")
            val fileUniqueId: String
        )
    }

}





