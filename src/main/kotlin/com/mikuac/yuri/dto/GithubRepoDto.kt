package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class GithubRepoDTO(
    @SerializedName("incomplete_results")
    val incompleteResults: Boolean,
    @SerializedName("total_count")
    val totalCount: Int,
    val items: List<Items>
) {
    data class Items(
        val private: Boolean,
        val name: String,
        val description: String,
        val language: String,
        val license: License? = null,
        @SerializedName("full_name")
        val fullName: String,
        @SerializedName("html_url")
        val htmlUrl: String,
        @SerializedName("forks_count")
        val forks: Int,
        @SerializedName("stargazers_count")
        val stars: Int,
        @SerializedName("default_branch")
        val defaultBranch: String,
    ) {
        data class License(
            @SerializedName("spdx_id")
            val spdxId: String? = null
        )
    }
}