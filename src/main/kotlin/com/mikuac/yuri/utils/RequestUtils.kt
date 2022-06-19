package com.mikuac.yuri.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

object RequestUtils {

    private val client = OkHttpClient()

    fun get(url: String): Response {
        val req = Request.Builder().url(url).get().build()
        return client.newCall(req).execute()
    }

    fun get(url: String, noReferer: Boolean): Response {
        val req = Request.Builder().url(url).get()
        if (noReferer) {
            req.header("referer", "no-referer")
        }
        return client.newCall(req.build()).execute()
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
        val req = Request.Builder().url(url).post(json.toRequestBody())
        headers.forEach {
            req.header(it.key, it.value)
        }
        return client.newCall(req.build()).execute()
    }

}
