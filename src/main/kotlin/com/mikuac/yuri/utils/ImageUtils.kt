package com.mikuac.yuri.utils

import com.mikuac.yuri.exception.YuriException
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object ImageUtils {

    private const val RAND_MOD_PX = 0b1
    private const val ROTATE_LEFT = 0b10
    private const val ROTATE_RIGHT = 0b100
    private const val ROTATE_DOWN = 0b1000

    fun imgToBase64(bytes: ByteArray): String {
        return "base64://${Base64.getEncoder().encodeToString(bytes)}"
    }

    fun formatPNG(imgURL: String, proxy: Boolean): String {
        return NetUtils.get(imgURL, proxy).use { resp ->
            val bufferedImage = ImageIO.read(resp.body?.byteStream())
            val out = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, extractImageFormat(imgURL), out)
            imgToBase64(out.toByteArray())
        }
    }

    fun imgAntiShielding(imgURL: String, mode: Int, proxy: Boolean): String {
        return NetUtils.get(imgURL, proxy).use { resp ->
            if (resp.code != 200) throw YuriException("图片获取失败：${resp.code}")
            var image = ImageIO.read(resp.body?.byteStream())
            if (mode and RAND_MOD_PX != 0) image = randomModifyPixels(image)
            if (mode and ROTATE_LEFT != 0) image = rotateImage(image, 90.00)
            else if (mode and ROTATE_RIGHT != 0) image = rotateImage(image, -90.00)
            else if (mode and ROTATE_DOWN != 0) image = rotateImage(image, 180.00)

            val out = ByteArrayOutputStream()
            ImageIO.write(image, extractImageFormat(imgURL), out)
            imgToBase64(out.toByteArray())
        }
    }

    private fun randomModifyPixels(image: BufferedImage): BufferedImage {
        val w = image.width
        val h = image.height
        val pixels = arrayOf(
            intArrayOf(0, 0),
            intArrayOf(w - 1, 0),
            intArrayOf(0, h - 1),
            intArrayOf(w - 1, h - 1)
        )
        val rand = Random()
        for ((x, y) in pixels) {
            val color = Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 255).rgb
            image.setRGB(x, y, color)
        }
        return image
    }

    private fun rotateImage(img: BufferedImage, angle: Double): BufferedImage {
        val radians = Math.toRadians(angle)
        val sin = abs(sin(radians))
        val cos = abs(cos(radians))
        val w = img.width
        val h = img.height
        val newWidth = (w * cos + h * sin).toInt()
        val newHeight = (h * cos + w * sin).toInt()
        val rotated = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB)
        val at = AffineTransform.getRotateInstance(radians, newWidth.toDouble() / 2, newHeight.toDouble() / 2)
        val g2d = rotated.createGraphics()
        g2d.transform = at
        g2d.drawImage(img, (newWidth - w) / 2, (newHeight - h) / 2, null)
        g2d.dispose()
        return rotated
    }

    private fun extractImageFormat(url: String): String {
        val regex = "\\.(jpg|jpeg|png)$".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.groupValues?.get(1) ?: "png"
    }

}