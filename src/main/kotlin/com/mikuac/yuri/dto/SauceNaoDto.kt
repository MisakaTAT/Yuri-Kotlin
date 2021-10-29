package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class SauceNaoDto(
    val header: Header,
    val results: List<Result>
) {
    data class Header(
        val account_type: String,
        @SerializedName("long_limit")
        val longLimit: String,
        val long_remaining: Int,
        val minimum_similarity: Int,
        val query_image: String,
        val query_image_display: String,
        val results_requested: Int,
        val results_returned: Int,
        val search_depth: String,
        val short_limit: String,
        val short_remaining: Int,
        val status: Int,
        val user_id: String
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

