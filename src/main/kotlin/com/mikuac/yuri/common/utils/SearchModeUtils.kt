package com.mikuac.yuri.common.utils

import com.mikuac.shiro.core.Bot
import com.mikuac.yuri.bean.SearchModeBean
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import java.util.concurrent.TimeUnit

class SearchModeUtils {

    companion object {

        val expiringMap: ExpiringMap<Long, SearchModeBean> = ExpiringMap.builder()
            .variableExpiration()
            .expiration(30, TimeUnit.SECONDS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .asyncExpirationListener { key: Long, value: SearchModeBean -> expCallBack(key, value) }
            .build()

        // 过期通知
        private fun expCallBack(key: Long, value: SearchModeBean) {
            MsgSendUtils.atSend(value.userId, value.groupId, value.bot, "您已经很久没有发送图片啦，帮您退出检索模式了哟～")
        }

        fun isSearchMode(key: Long): Boolean {
            return expiringMap[key] != null
        }

        fun setSearchMode(key: Long, mode: String, userId: Long, groupId: Long, bot: Bot) {
            var nativeMode = mode
            when (mode) {
                "WhatAnime" -> nativeMode = "番"
            }
            if (isSearchMode(key)) {
                MsgSendUtils.atSend(userId, groupId, bot, "您已经处于搜${nativeMode}模式啦，请直接发送需要检索的图片~")
                return
            }
            val info = SearchModeBean(userId = userId, groupId = groupId, mode = mode, bot)
            expiringMap[key] = info
            MsgSendUtils.atSend(userId, groupId, bot, "您已进入搜${nativeMode}模式，请发送您想查找的图片试试吧～")
        }

    }

}