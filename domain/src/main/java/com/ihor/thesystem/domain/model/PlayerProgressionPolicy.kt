package com.ihor.thesystem.domain.model

data class MainQuestProgressionResult(
    val player: Player,
    val penaltyActivated: Boolean
)

object PlayerProgressionPolicy {
    const val DEFAULT_PENALTY_FAILURE_THRESHOLD = 2

    fun applyMainQuestSuccess(
        player: Player,
        reward: Boolean,
        progressionConfig: PlayerProgressionConfig = PlayerProgressionConfig()
    ): MainQuestProgressionResult {
        val rewardedPlayer = if (reward) {
            player.rewardWorkoutCompletion(progressionConfig)
        } else {
            player
        }
        return MainQuestProgressionResult(
            player = rewardedPlayer.copy(
                consecutiveMainQuestFailures = 0,
                isPenaltyActive = false
            ),
            penaltyActivated = false
        )
    }

    fun applyMainQuestFailure(
        player: Player,
        failureThreshold: Int = DEFAULT_PENALTY_FAILURE_THRESHOLD
    ): MainQuestProgressionResult {
        val threshold = failureThreshold.coerceAtLeast(1)
        val failures = player.consecutiveMainQuestFailures + 1
        val wasPenaltyActive = player.isPenaltyActive
        val isPenaltyActive = failures >= threshold
        return MainQuestProgressionResult(
            player = player.copy(
                currentStreak = 0,
                consecutiveMainQuestFailures = failures,
                isPenaltyActive = isPenaltyActive
            ),
            penaltyActivated = !wasPenaltyActive && isPenaltyActive
        )
    }

    fun applyMissedScheduledWorkout(player: Player): Player =
        player.copy(currentStreak = 0)
}
