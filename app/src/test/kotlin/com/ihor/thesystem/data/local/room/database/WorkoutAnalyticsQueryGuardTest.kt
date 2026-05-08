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

        assertFalse(
            "Weight history must not select MAX(weight) beside a non-aggregated session timestamp.",
            "SELECT MAX(weight) as weight, s.timestamp" in daoSource
        )
        assertTrue(
            "Weight history must resolve the timestamp from rows that match the daily max weight.",
            "WITH daily_max AS" in daoSource &&
                "AND e.weight = daily_max.weight" in daoSource
        )
    }

    private fun projectRoot(): File =
        File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
}
