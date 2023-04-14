package com.mikuac.yuri.utils

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mikuac.yuri.config.Config

object TelegramUtils {

    private val cfg = Config.plugins.telegram

    fun getFile(fileId: String, proxy: Boolean): String {
        val api = "https://api.telegram.org"
        return NetUtils.get("${api}/bot${cfg.botToken}/getFile?file_id=${fileId}", proxy).use { resp ->
            val data = Gson().fromJson(resp.body?.string(), Result::class.java)
            if (data.ok) return "${api}/file/bot${cfg.botToken}/${data.result.filePath}"
            ""
        }
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





