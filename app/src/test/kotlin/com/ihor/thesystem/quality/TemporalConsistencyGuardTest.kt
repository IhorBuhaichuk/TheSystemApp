package com.ihor.thesystem.quality

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TemporalConsistencyGuardTest {

    @Test
    fun `critical domain and data paths use AppClock for current time and timezone`() {
        val appRoot = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        val repoRoot = requireNotNull(appRoot.parentFile)
        val relativePaths = listOf(
            "app/src/main/java/com/ihor/thesystem/data/repository_impl/ProgressionMatrixRepositoryImpl.kt",
            "app/src/main/java/com/ihor/thesystem/data/repository_impl/QuestRepositoryImpl.kt",
            "app/src/main/java/com/ihor/thesystem/data/repository_impl/WorkoutAnalyticsRepositoryImpl.kt",
            "domain/src/main/java/com/ihor/thesystem/domain/usecase/GetDailySummaryForDateUseCase.kt",
            "domain/src/main/java/com/ihor/thesystem/domain/usecase/GetPlayerWeightContextUseCase.kt",
            "domain/src/main/java/com/ihor/thesystem/domain/usecase/GetWorkoutAnalysisUseCase.kt",
            "domain/src/main/java/com/ihor/thesystem/domain/usecase/LogWorkoutSetsUseCase.kt",
            "app/src/main/java/com/ihor/thesystem/feature/statistics/viewmodel/StatisticsViewModel.kt",
            "app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/StatusViewModel.kt",
            "app/src/main/java/com/ihor/thesystem/feature/status/viewmodel/WorkoutViewModel.kt"
        )
        val forbidden = listOf("System.currentTimeMillis()", "ZoneId.systemDefault()")

        val offenders = relativePaths
            .map { repoRoot.resolve(it) }
            .filter { it.exists() }
            .flatMap { file ->
                val path = file.relativeTo(repoRoot).invariantSeparatorsPath
                file.readLines().mapIndexedNotNull { index, line ->
                    forbidden.firstOrNull { it in line }?.let { usage ->
                        "$path:${index + 1} uses $usage"
                    }
                }
            }

        assertTrue(
            "Use injected AppClock for critical date grouping, logs, and ViewModel timestamps: $offenders",
            offenders.isEmpty()
        )
    }
}
