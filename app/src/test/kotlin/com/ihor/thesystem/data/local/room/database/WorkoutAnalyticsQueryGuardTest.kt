package com.ihor.thesystem.data.local.room.database

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WorkoutAnalyticsQueryGuardTest {

    @Test
    fun `session by date query uses timestamp range instead of sqlite date conversion`() {
        val daoSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt")
            .readText()
        val repositorySource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/repository_impl/WorkoutAnalyticsRepositoryImpl.kt")
            .readText()

        assertTrue(
            "Session-by-date DAO query must be a timestamp range query.",
            "WHERE timestamp BETWEEN :startInclusive AND :endInclusive" in daoSource
        )
        assertFalse(
            "Session-by-date DAO query must not call date(timestamp...), because it defeats the timestamp index.",
            "WHERE date(timestamp / 1000" in daoSource
        )
        assertTrue(
            "WorkoutAnalyticsRepositoryImpl must compute day bounds with AppClock before querying Room.",
            "clock.zoneId()" in repositorySource &&
                "getSessionLogsBetween(startOfDay, endOfDay)" in repositorySource
        )
    }

    @Test
    fun `exercise weight history does not pair aggregated max weight with arbitrary timestamp`() {
        val daoSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt")
            .readText()
        val repositorySource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/repository_impl/WorkoutAnalyticsRepositoryImpl.kt")
            .readText()

        assertFalse(
            "Weight history must not select MAX(weight) beside a non-aggregated session timestamp.",
            "SELECT MAX(weight) as weight, s.timestamp" in daoSource
        )
        assertFalse(
            "Weight history must not group user days through SQLite UTC date conversion.",
            "date(s.timestamp / 1000, 'unixepoch')" in daoSource
        )
        assertTrue(
            "Weight history must resolve local-day max weight in the AppClock-aware repository.",
            "WorkoutAnalyticsLocalDayGrouper.dailyMaxWeightHistory" in repositorySource
        )
    }

    @Test
    fun `daily tonnage aggregation does not use sqlite UTC date boundary`() {
        val daoSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt")
            .readText()
        val repositorySource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/repository_impl/WorkoutAnalyticsRepositoryImpl.kt")
            .readText()

        assertFalse(
            "Daily tonnage must not group through SQLite UTC date conversion.",
            "GROUP BY date(timestamp / 1000, 'unixepoch')" in daoSource
        )
        assertTrue(
            "WorkoutAnalyticsRepositoryImpl must group tonnage by AppClock local day.",
            "WorkoutAnalyticsLocalDayGrouper.groupTonnageByLocalDay" in repositorySource
        )
    }

    @Test
    fun `last workout sets support one batch query for all displayed exercises`() {
        val daoSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt")
            .readText()
        val repositorySource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/repository_impl/WorkoutAnalyticsRepositoryImpl.kt")
            .readText()

        assertTrue(
            "Displayed workout recommendations must load previous sets with one IN query.",
            "getLastSetsForExercises" in daoSource &&
                "e.exerciseId IN (:exerciseIds)" in daoSource
        )
        assertTrue(
            "The repository must group the single batch result by exercise.",
            "dao.getLastSetsForExercises(exerciseIds.distinct())" in repositorySource &&
                ".groupBy { it.exerciseId }" in repositorySource
        )
    }

    @Test
    fun `statistics workout logs are bounded by the relevant time window`() {
        val daoSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt")
            .readText()
        val repositorySource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/repository_impl/WorkoutAnalyticsRepositoryImpl.kt")
            .readText()
        val useCaseSource = projectRoot().parentFile
            .resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/GetStatisticsDataUseCase.kt")
            .readText()

        assertTrue(
            "Statistics sessions must use the indexed timestamp range and a hard row cap.",
            "getSessionLogsForStatistics" in daoSource &&
                "timestamp >= :startInclusive AND timestamp < :endExclusive" in daoSource &&
                "LIMIT 200" in daoSource
        )
        assertTrue(
            "The repository must forward the Statistics time window to Room.",
            "dao.getSessionLogsForStatistics(startInclusive, endExclusive)" in repositorySource
        )
        assertFalse(
            "The Statistics critical flow must not load the general session history.",
            "analyticsRepo.getAllLogs()" in useCaseSource
        )
        assertTrue(
            "The Statistics critical flow must request only its relevant comparison window.",
            "analyticsRepo.getLogsBetween(" in useCaseSource &&
                "buildProgressProofs.relevantPeriodStartMillis()" in useCaseSource
        )
    }

    @Test
    fun `annual progression history is bounded and preserves one prior baseline`() {
        val daoSource = projectRoot()
            .resolve("src/main/java/com/ihor/thesystem/data/local/room/dao/WorkoutAnalyticsDao.kt")
            .readText()
        val annualUseCaseSource = projectRoot().parentFile
            .resolve("domain/src/main/java/com/ihor/thesystem/domain/usecase/GetAnnualProgressionDetailsUseCase.kt")
            .readText()

        assertTrue(
            "Annual history must load records from the earliest active plan start.",
            "getWeightHistoriesBetween" in daoSource &&
                "WHERE s.timestamp >= :startInclusive AND s.timestamp < :endExclusive" in daoSource &&
                "getWeightHistoriesBetween(" in annualUseCaseSource
        )
        assertTrue(
            "Annual history must preserve one pre-plan baseline per exercise.",
            "MAX(s2.timestamp) AS latestTimestamp" in daoSource &&
                "WHERE s2.timestamp < :startInclusive" in daoSource
        )
        assertFalse(
            "Annual progression must not observe the unbounded all-history query.",
            "getAllWeightHistories()" in annualUseCaseSource
        )
    }

    private fun projectRoot(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
}
