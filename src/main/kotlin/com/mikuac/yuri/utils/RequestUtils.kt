package com.mikuac.yuri.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

@Suppress("unused")
object RequestUtils {

    private val client = OkHttpClient()

    fun get(url: String): Response {
        val req = Request.Builder().url(url).get().build()
        val resp = client.newCall(req).execute()
        resp.close()
        return resp
    }

    fun get(url: String, noReferer: Boolean): Response {
        val req = Request.Builder().url(url).get()
        if (noReferer) {
            req.header("referer", "no-referer")
        }
        val resp = client.newCall(req.build()).execute()
        resp.close()
        return resp
    }

    fun get(url: String, headers: Map<String, String>): Response {
        val req = Request.Builder().url(url).get()
        headers.forEach {
            req.header(it.key, it.value)
        }
        val resp = client.newCall(req.build()).execute()
        resp.close()
        return resp
    }

    fun post(url: String, json: String): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder().url(url).post(json.toRequestBody(mediaType))
        val resp = client.newCall(request.build()).execute()
        resp.close()
        return resp
    }

    fun post(url: String, json: String, headers: Map<String, String>): Response {
        val req = Request.Builder().url(url).post(json.toRequestBody())
        headers.forEach {
            req.header(it.key, it.value)
        }
        val resp = client.newCall(req.build()).execute()
        resp.close()
        return resp
    }

}
