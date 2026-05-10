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

    private fun projectRoot(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
}
