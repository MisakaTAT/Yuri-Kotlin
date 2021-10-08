package com.mikuac.yuri.common.utils

import cn.hutool.http.HttpRequest
import mu.KotlinLogging

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

    }

}
