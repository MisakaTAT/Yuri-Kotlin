package com.mikuac.yuri.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.springframework.http.HttpMethod
import java.net.HttpURLConnection
import java.net.URL

object RequestUtils {

    private val client = OkHttpClient()

    fun get(url: String): ResponseBody? {
        val request = Request.Builder().url(url).get().build()
        val call = client.newCall(request)
        return call.execute().body
    }

    fun get(url: String, noReferer: Boolean): ResponseBody? {
        val request = Request.Builder().url(url).get()
        if (noReferer) {
            request.header("referer", "no-referer")
        }
        val call = client.newCall(request.build())
        return call.execute().body
    }

    fun post(url: String, json: String): ResponseBody? {
        val request = Request.Builder().url(url).post(json.toRequestBody())
        val call = client.newCall(request.build())
        return call.execute().body
    }

    fun post(url: String, json: String, headers: Map<String, String>): ResponseBody? {
        val request = Request.Builder().url(url).post(json.toRequestBody())
        headers.forEach {
            request.header(it.key, it.value)
        }
        val call = client.newCall(request.build())
        return call.execute().body
    }

    fun findLink(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = HttpMethod.GET.name
        conn.connect()
        var location = conn.getHeaderField("Location")
        val code = conn.responseCode
        location = if (code in 301..302) {
            findLink(location)
        } else {
            conn.url.toString()
        }
        conn.disconnect()
        return location
    }

}
