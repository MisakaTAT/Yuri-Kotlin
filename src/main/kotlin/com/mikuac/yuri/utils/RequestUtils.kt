package com.mikuac.yuri.utils

import cn.hutool.http.HttpRequest
import org.springframework.http.HttpMethod
import java.net.HttpURLConnection
import java.net.URL

class RequestUtils {

    companion object {

        fun get(url: String): String {
            return HttpRequest(url).execute().body()
        }

        fun get(url: String, noReferer: Boolean): String {
            val httpRequest = HttpRequest(url)
            if (noReferer) {
                httpRequest.header("referer", "no-referer")
            }
            return httpRequest.execute().body()
        }

        fun post(url: String, json: String): String? {
            return HttpRequest.post(url).body(json).execute().body()
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

}
