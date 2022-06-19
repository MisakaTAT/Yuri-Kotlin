package com.mikuac.yuri.bean.dto

import com.google.gson.annotations.SerializedName

data class SauceNaoDto(
    val header: Header,
    val results: List<Result>
) {
    data class Header(
        @SerializedName("account_type")
        val accountType: String,
        @SerializedName("long_limit")
        val longLimit: String,
        @SerializedName("long_remaining")
        var longRemaining: Int,
        @SerializedName("short_limit")
        val shortLimit: String,
        @SerializedName("short_remaining")
        val shortRemaining: Int,
        @SerializedName("user_id")
        val userId: String,
        val status: Int
    )

    data class Result(
        val data: Data,
        val header: Header
    ) {
        data class Data(
            @SerializedName("ext_urls")
            val extUrls: List<String>,
            @SerializedName("member_id")
            val authorId: Long,
            @SerializedName("member_name")
            val authorName: String,
            @SerializedName("pixiv_id")
            val pixivId: Long,
            val title: String,
            val source: String,
            @SerializedName("eng_name")
            val engName: String,
            @SerializedName("jp_name")
            val jpName: String,
            @SerializedName("tweet_id")
            val tweetId: String,
            @SerializedName("twitter_user_id")
            val twitterUserId: String,
            @SerializedName("twitter_user_handle")
            val twitterUserHandle: String
        )

        data class Header(
            @SerializedName("index_id")
            val indexId: Int,
            val similarity: String,
            val thumbnail: String
        )
    }
}

