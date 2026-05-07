package com.ihor.thesystem.domain.model

import kotlin.math.roundToInt

object RankProgressionPolicy {
    private val orderedRanks: List<Rank> = Rank.entries.sortedBy { it.weight }
    private val minRankWeight: Int = orderedRanks.first().weight
    private val maxRankWeight: Int = orderedRanks.last().weight

    fun nextRank(current: Rank): Rank {
        return orderedRanks.firstOrNull { it.weight > current.weight } ?: current
    }

    fun shouldPromote(current: Rank, candidate: Rank): Boolean {
        return candidate.weight > current.weight
    }

    fun resolveGlobalRank(
        ranks: List<Rank>,
        topCount: Int = DEFAULT_GLOBAL_RANK_TOP_COUNT
    ): Rank? {
        require(topCount > 0) { "Global rank top count must be positive." }
        if (ranks.isEmpty()) return null

        val topRankWeights = ranks
            .map { it.weight }
            .sortedDescending()
            .take(topCount)

        val mid = topRankWeights.size / 2
        val medianWeight = if (topRankWeights.size % 2 == 0) {
            ((topRankWeights[mid - 1] + topRankWeights[mid]) / 2.0).roundToInt()
        } else {
            topRankWeights[mid]
        }

        return Rank.fromValue(medianWeight.coerceIn(minRankWeight, maxRankWeight))
    }
}

private const val DEFAULT_GLOBAL_RANK_TOP_COUNT = 5
