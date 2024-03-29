@file:Suppress("SpellCheckingInspection")

package com.mikuac.yuri.utils

import com.mikuac.yuri.annotation.Slf4j.Companion.log
import com.mikuac.yuri.exception.YuriException
import lombok.extern.slf4j.Slf4j
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
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


    /**
     * ffmpeg -y -i src.webm -vf "scale=320:-1" -r 10 dst.gif
     */
    fun webm2Gif(src: String): String {
        return File(src).let { file ->
            if (!file.name.endsWith("webm")) throw YuriException("不支持的文件格式：${file.extension}")
            val dst = "${file.parent}/${file.nameWithoutExtension}.gif"
            val process = ProcessBuilder()
                .command(ffmpeg(), "-y", "-i", src, "-vf", "scale=320:-1", "-r", "10", dst)
                .redirectErrorStream(true)
                .start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = reader.readLine()
            var lastLine: String? = null
            while (line != null) {
                lastLine = line
                line = reader.readLine()
            }
            process.waitFor().let {
                if (it != 0) {
                    log.error("WebM2Gif Failed: $lastLine")
                    throw YuriException("WebM2Gif Failed")
                }
                dst
            }
        }
    }

}