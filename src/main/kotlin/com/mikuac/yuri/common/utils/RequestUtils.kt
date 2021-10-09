package com.mikuac.yuri.common.utils

import cn.hutool.http.HttpRequest
import mu.KotlinLogging
import org.springframework.http.HttpMethod
import java.net.HttpURLConnection
import java.net.URL

class RequestUtils {

    companion object {

        private val log = KotlinLogging.logger {}

        fun get(url: String): String? {
            return try {
                val exec = HttpRequest(url).execute()
                if (exec.status != 200) return null
                exec.body()
            } catch (e: Exception) {
                log.error("Request utils get exception: ${e.message}")
                null
            }
        }

        fun get(url: String, noReferer: Boolean): String? {
            return try {
                val httpRequest = HttpRequest(url)
                if (noReferer) {
                    httpRequest.header("referer", "no-referer")
                }
                val exec = httpRequest.execute()
                if (exec.status != 200) return null
                exec.body()
            } catch (e: Exception) {
                log.error("Request utils get exception: ${e.message}")
                null
            }
        }

        fun post(url: String, json: String): String? {
            return try {
                val exec = HttpRequest.post(url)
                    .body(json)
                    .execute()
                if (exec.status != 200) return null
                exec.body()
            } catch (e: Exception) {
                log.error("Request utils post exception: ${e.message}")
                null
            }
        }

        fun findLink(url: String): String? {
            try {
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
            } catch (e: Exception) {
                log.error("Request utils find link exception: ${e.message}")
            }
            return null
        }

    }

}
