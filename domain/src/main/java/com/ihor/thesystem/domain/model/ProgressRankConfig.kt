package com.ihor.thesystem.domain.model

data class ProgressRankConfig(
    val thresholds: List<ProgressRankThreshold> = defaultProgressRankThresholds
) {
    init {
        require(thresholds.isNotEmpty()) { "Progress rank thresholds must not be empty." }
        require(thresholds.first().minProgress <= 0.0) {
            "Progress rank thresholds must cover baseline progress."
        }
    }
}

data class ProgressRankThreshold(
    val minProgress: Double,
    val rank: Rank
)

val defaultProgressRankThresholds = listOf(
    ProgressRankThreshold(0.0, Rank.E),
    ProgressRankThreshold(0.2, Rank.D),
    ProgressRankThreshold(0.4, Rank.C),
    ProgressRankThreshold(0.6, Rank.B),
    ProgressRankThreshold(0.8, Rank.A),
    ProgressRankThreshold(1.0, Rank.S)
)
