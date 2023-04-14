package com.mikuac.yuri.utils

import cn.hutool.core.io.FileUtil
import com.mikuac.yuri.config.Config
import com.mikuac.yuri.exception.YuriException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

@Suppress("unused")
object NetUtils {

    private val cfg = Config.base

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient()

    private fun createProxy(): Proxy {
        return Proxy(Proxy.Type.valueOf(cfg.proxy.type), InetSocketAddress(cfg.proxy.host, cfg.proxy.port))
    }

    fun get(url: String): Response {
        val req = Request.Builder().url(url).get().build()
        return client.newCall(req).execute()
    }

    fun get(url: String, proxy: Boolean): Response {
        val req = Request.Builder().url(url).get()
        val client = client.newBuilder()
        if (proxy) client.proxy(createProxy())
        return client.build().newCall(req.build()).execute()

    }

    fun get(url: String, headers: Map<String, String>): Response {
        val req = Request.Builder().url(url).get()
        headers.forEach {
            req.header(it.key, it.value)
        }
        return client.newCall(req.build()).execute()
    }

    fun download(url: String, path: String, name: String, proxy: Boolean): String {
        File(path).let { if (!it.exists()) FileUtil.mkdir(it) }
        val req = Request.Builder().url(url).build()
        val client = client.newBuilder()
        if (proxy) client.proxy(createProxy())
        val resp = client.build().newCall(req).execute()
        val inputStream = resp.body?.byteStream()
        val file = File(path, name)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun download(url: String, path: String, name: String, timeout: Int): String {
        File(path).let { if (!it.exists()) FileUtil.mkdir(it) }
        val req = Request.Builder().url(url).build()
        val resp = client.newBuilder().readTimeout(timeout.toLong(), TimeUnit.SECONDS).build().newCall(req).execute()
        resp.code.let { if (it != 200) throw YuriException("请求处理失败：${it}") }
        val inputStream = resp.body?.byteStream()
        val file = File(path, name)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun post(url: String, json: String): Response {
        val request = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        return client.newCall(request.build()).execute()
    }

    fun post(url: String, json: String, headers: Map<String, String>): Response {
        val req = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        headers.forEach {
            req.header(it.key, it.value)
        }
        return client.newCall(req.build()).execute()
    }

    fun post(url: String, headers: Map<String, String>, json: String, proxy: Boolean, readTimeout: Long): Response {
        val req = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        headers.forEach {
            req.header(it.key, it.value)
        }
        val client = client.newBuilder()
        if (proxy) client.proxy(createProxy())
        return client.readTimeout(readTimeout, TimeUnit.SECONDS).build().newCall(req.build()).execute()
    }

    fun post(url: String, json: String, proxy: Boolean): Response {
        val req = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        val client = client.newBuilder()
        if (proxy) client.proxy(createProxy())
        return client.build().newCall(req.build()).execute()
    }

}
