package com.mikuac.yuri.utils

import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

object ImageUtils {

    @JvmStatic
    fun imgToBase64(bytes: ByteArray): String {
        return "base64://${Base64.getEncoder().encodeToString(bytes)}"
    }

    @JvmStatic
    fun formatPNG(imgURL: String, proxy: Boolean): String {
        val resp = NetUtils.get(imgURL, proxy)
        val bufferedImage = ImageIO.read(resp.body?.byteStream())
        resp.close()
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream)
        return imgToBase64(byteArrayOutputStream.toByteArray())
    }

}