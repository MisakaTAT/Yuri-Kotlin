package com.mikuac.yuri.dto

data class EroticPicDto(
    val data: List<Data>,
    val error: String
) {
    data class Data(
        val author: String,
        val ext: String,
        val height: Int,
        val p: Int,
        val pid: Int,
        val r18: Boolean,
        val tags: List<String>,
        val title: String,
        val uid: Int,
        val uploadDate: Long,
        val urls: Urls,
        val width: Int
    )

    data class Urls(
        val original: String
    )
}
