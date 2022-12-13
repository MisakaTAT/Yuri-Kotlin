package com.mikuac.yuri.utils

import com.mikuac.yuri.config.Config
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

@Suppress("unused")
object NetUtils {

    private val client = OkHttpClient()

    fun get(url: String): Response {
        val req = Request.Builder().url(url).get().build()
        return client.newCall(req).execute()
    }

    fun get(url: String, proxy: Boolean): Response {
        val req = Request.Builder().url(url).get()
        val client = client.newBuilder()
        if (proxy) {
            client.proxy(
                Proxy(
                    Proxy.Type.valueOf(Config.base.proxy.type),
                    InetSocketAddress(Config.base.proxy.host, Config.base.proxy.port)
                )
            )
        }
        return client.build().newCall(req.build()).execute()

    }

    fun get(url: String, headers: Map<String, String>): Response {
        val req = Request.Builder().url(url).get()
        headers.forEach {
            req.header(it.key, it.value)
        }
        return client.newCall(req.build()).execute()
    }

    fun post(url: String, json: String): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        return client.newCall(request.build()).execute()
    }

    fun post(url: String, json: String, headers: Map<String, String>): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val req = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        headers.forEach {
            req.header(it.key, it.value)
        }
        return client.newCall(req.build()).execute()
    }

    fun post(url: String, headers: Map<String, String>, json: String, proxy: Boolean, readTimeout: Long): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val req = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        headers.forEach {
            req.header(it.key, it.value)
        }
        val client = client.newBuilder()
        if (proxy) {
            client.proxy(
                Proxy(
                    Proxy.Type.valueOf(Config.base.proxy.type),
                    InetSocketAddress(Config.base.proxy.host, Config.base.proxy.port)
                )
            )
        }
        return client.readTimeout(readTimeout, TimeUnit.SECONDS).build().newCall(req.build()).execute()
    }

    fun post(url: String, json: String, proxy: Boolean): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val req = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        val client = client.newBuilder()
        if (proxy) {
            client.proxy(
                Proxy(
                    Proxy.Type.valueOf(Config.base.proxy.type),
                    InetSocketAddress(Config.base.proxy.host, Config.base.proxy.port)
                )
            )
        }
        return client.build().newCall(req.build()).execute()
    }

}
