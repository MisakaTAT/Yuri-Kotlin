@file:Suppress("SpellCheckingInspection")

package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

class VitsDTO {
    data class Speakers(
            @SerializedName("VITS")
            val vits: List<Map<String, String>>
    )
}
