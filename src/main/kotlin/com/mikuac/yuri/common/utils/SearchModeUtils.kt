package com.mikuac.yuri.common.utils

import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

class SearchModeUtils {

    companion object {

        val expiringMap: ExpiringMap<Long, String> = ExpiringMap.builder()
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(30, TimeUnit.SECONDS)
            .build()

        fun getCurrentMode(key: Long): String? {
            return expiringMap[key]
        }

        fun isSearchMode(key: Long): Boolean {
            return expiringMap[key] != null
        }

    }

}