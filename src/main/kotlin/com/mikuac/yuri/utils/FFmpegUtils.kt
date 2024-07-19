@file:Suppress("SpellCheckingInspection")

package com.mikuac.yuri.utils

import com.mikuac.yuri.exception.YuriException
import lombok.extern.slf4j.Slf4j
import java.io.File
import java.io.IOException
import java.util.*

@Slf4j
object FFmpegUtils {

    private fun ffmpeg(): String {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        return if (osName.contains("win")) "ffmpeg.exe" else "ffmpeg"
    }

    fun ffmpegCheck(): Boolean {
        val processBuilder = ProcessBuilder(ffmpeg(), "-version")
        return try {
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.waitFor()
            output.contains("ffmpeg version") || error.contains("ffmpeg version")
        } catch (e: IOException) {
            false
        }
    }

    fun webm2Gif(src: String): String {
        return File(src).let { file ->
            if (!file.name.endsWith("webm")) throw YuriException("不支持的文件格式：${file.extension}")
            val dst = "${file.parent}/${file.nameWithoutExtension}.gif"

            val cache = File(dst)
            if (cache.exists()) {
                return@let dst
            }

            val p1 = ProcessBuilder(
                ffmpeg(),
                "-vcodec", "libvpx-vp9",
                "-i", src,
                "-pix_fmt", "rgba",
                "-plays", "0",
                "-f", "apng",
                "-"
            ).start()


            val p2 = ProcessBuilder(
                ffmpeg(),
                "-f", "apng",
                "-i", "-",
                "-lavfi", "split[v],palettegen,[v]paletteuse",
                "-y",
                dst
            ).start()

            p1.inputStream.copyTo(p2.outputStream)
            p2.outputStream.close()

            if (p1.waitFor() != 0 || p2.waitFor() != 0) {
                throw YuriException("WebM to GIF conversion failed")
            }

            return@let dst
        }
    }

}