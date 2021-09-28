package com.mikuac.yuri.common.utils

import cn.hutool.http.HttpRequest
import com.mikuac.yuri.common.log.Slf4j.Companion.log

class RequestUtils {

    companion object {

        fun get(url: String): String? {
            return try {
                val exec = HttpRequest(url).execute()
                if (exec.status != 200) return null
                exec.body()
            } catch (e: Exception) {
                log.error("RequestUtils Get 请求异常：${e.message}")
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
                log.error("RequestUtils Get 请求异常：${e.message}")
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
                log.error("RequestUtils Post 请求异常：${e.message}")
                null
            }
        }

    }

}
