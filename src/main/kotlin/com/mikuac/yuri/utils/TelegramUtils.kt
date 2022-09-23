package com.mikuac.yuri.utils

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mikuac.yuri.config.Config
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

object TelegramUtils {

    @JvmStatic
    fun imgToBase64(bytes: ByteArray): String {
        return "base64://${Base64.getEncoder().encodeToString(bytes)}"
    }

    @JvmStatic
    fun formatPNG(imgURL: String): String {
        val resp = RequestUtils.proxyGet(imgURL)
        val bufferedImage = ImageIO.read(resp.body?.byteStream())
        resp.close()
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
        return imgToBase64(byteArrayOutputStream.toByteArray())
    }

    @JvmStatic
    fun getFile(fileId: String): String? {
        val baseURL = "https://api.telegram.org"
        val botToken = Config.plugins.telegram.botToken
        val api = "${baseURL}/bot${botToken}/getFile?file_id=${fileId}"
        val resp = RequestUtils.proxyGet(api)
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
            @SerializedName("file_id") val fileId: String,
            @SerializedName("file_path") val filePath: String,
            @SerializedName("file_size") val fileSize: Int,
            @SerializedName("file_unique_id") val fileUniqueId: String
        )
    }

}





