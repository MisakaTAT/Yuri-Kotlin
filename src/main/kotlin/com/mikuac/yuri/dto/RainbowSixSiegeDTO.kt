package com.mikuac.yuri.dto

import com.google.gson.annotations.SerializedName

data class RainbowSixSiegeDTO(
    val status: Int,
    val username: String,
    @SerializedName("Basicstat")
    val basicStat: List<BasicStat>,
    @SerializedName("StatGeneral")
    val statGeneral: List<StatGeneral>,
    @SerializedName("StatBHS")
    val statBHS: List<StatBHS>,
    @SerializedName("StatCR")
    val statCR: List<StatCR>,
) {
    data class BasicStat(
        val abandons: Int,
        val deaths: Int,
        val id: String,
        val kills: Int,
        @SerializedName("last_match_mmr_change")
        val lastMatchMmrChange: Int,
        val level: Int,
        val losses: Int,
        @SerializedName("max_mmr")
        val maxMmr: Double,
        @SerializedName("max_rank")
        val maxRank: Int,
        val mmr: Double,
        val platform: String,
        val rank: Int,
        val region: String,
        val season: Int,
        @SerializedName("skill_mean")
        val skillMean: Double,
        @SerializedName("skill_stdev")
        val skillStDev: Double,
        @SerializedName("top_rank_position")
        val topRankPosition: Int,
        @SerializedName("updated_at")
        val updatedAt: String,
        val wins: Int
    )

    data class StatGeneral(
        val bulletsFired: Int,
        val bulletsHit: Int,
        val deaths: Int,
        val headshot: Int,
        val id: String,
        val killAssists: Int,
        val kills: Int,
        val lost: Int,
        val meleeKills: Int,
        val penetrationKills: Int,
        val played: Int,
        val revives: Int,
        val timePlayed: Int,
        val won: Int
    )

    data class StatCR(
        val deaths: Int,
        val id: String,
        val kills: Int,
        val lost: Int,
        val mmr: Double,
        val model: String,
        val played: Int,
        val timePlayed: Int,
        val won: Int
    )

    data class StatBHS(
        @SerializedName("bestscore")
        val bestScore: Int,
        val id: String,
        val lost: Int,
        val model: String,
        val played: Int,
        val won: Int
    )
}