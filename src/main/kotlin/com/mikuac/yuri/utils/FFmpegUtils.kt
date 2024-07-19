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
            val outputDir = "${file.parent}/frames"
            val dst = "${file.parent}/${file.nameWithoutExtension}.gif"

            val cache = File(dst)
            if (cache.exists()) {
                return@let dst
            }

            File(outputDir).mkdirs()

            // 将 WebM 文件切成 PNG 序列
            val extractProcess = ProcessBuilder()
                .command(
                    ffmpeg(),
                    "-vcodec",
                    "libvpx-vp9",
                    "-i",
                    src,
                    "-pix_fmt",
                    "rgba",
                    "$outputDir/%04d.png"
                )
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()

            extractProcess.waitFor().let {
                if (it != 0) {
                    throw YuriException("WebM to PNG extraction failed")
                }
            }

            // 将 PNG 序列合并为 GIF
            val mergeProcess = ProcessBuilder()
                .command(
                    ffmpeg(),
                    "-i",
                    "$outputDir/%04d.png",
                    "-lavfi",
                    "split[v],palettegen,[v]paletteuse",
                    dst
                )
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()

            mergeProcess.waitFor().let {
                if (it != 0) {
                    throw YuriException("PNG to GIF merge failed")
                }
            }

            File(outputDir).deleteRecursively()
            return@let dst
        }
    }

}