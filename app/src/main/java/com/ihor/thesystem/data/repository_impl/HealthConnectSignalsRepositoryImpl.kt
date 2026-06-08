package com.ihor.thesystem.data.repository_impl

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.HealthPermissionRequest
import com.ihor.thesystem.domain.model.HealthSignalPermission
import com.ihor.thesystem.domain.model.HealthSignals
import com.ihor.thesystem.domain.model.HealthSignalsFreshness
import com.ihor.thesystem.domain.repository.HealthSignalsRepository
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.health.HealthConnectPermissions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

@Singleton
class HealthConnectSignalsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider
) : HealthSignalsRepository {

    override suspend fun isAvailable(): Boolean =
        withContext(dispatchers.io) {
            sdkAvailable()
        }

    override suspend fun hasPermissions(required: Set<HealthSignalPermission>): Boolean =
        withContext(dispatchers.io) {
            val client = clientOrNull() ?: return@withContext false
            val requiredPermissions = HealthConnectPermissions.permissionsFor(required)
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(requiredPermissions)
        }

    override fun requestPermissions(required: Set<HealthSignalPermission>): HealthPermissionRequest =
        HealthPermissionRequest(required)

    override suspend fun getTodaySignals(): HealthSignals =
        withContext(dispatchers.io) {
            val today = today()
            readSignalsForDate(today)
        }

    override suspend fun getRecentSignals(days: Int): List<HealthSignals> =
        withContext(dispatchers.io) {
            val boundedDays = days.coerceIn(1, MAX_RECENT_DAYS)
            val today = today()
            (0 until boundedDays).map { offset ->
                readSignalsForDate(today.minusDays(offset.toLong()))
            }
        }

    private suspend fun readSignalsForDate(date: LocalDate): HealthSignals {
        val client = clientOrNull() ?: return HealthSignals.Unavailable
        val granted = safeHealthRead { client.permissionController.getGrantedPermissions() }
            ?: return HealthSignals.Unavailable
        if (granted.isEmpty()) return HealthSignals.Unavailable

        val start = date.atStartOfDay(clock.zoneId()).toInstant()
        val end = if (date == today()) {
            Instant.ofEpochMilli(clock.now())
        } else {
            date.plusDays(1).atStartOfDay(clock.zoneId()).toInstant()
        }
        val range = TimeRangeFilter.between(start, end)

        val sleepPermission = HealthConnectPermissions.permissionsFor(setOf(HealthSignalPermission.SLEEP)).single()
        val stepsPermission = HealthConnectPermissions.permissionsFor(setOf(HealthSignalPermission.STEPS)).single()
        val heartRatePermission = HealthConnectPermissions.permissionsFor(setOf(HealthSignalPermission.HEART_RATE)).single()
        val exercisePermission = HealthConnectPermissions.permissionsFor(setOf(HealthSignalPermission.EXERCISE_SESSIONS)).single()

        return HealthSignals(
            sleepDurationMinutes = if (sleepPermission in granted) {
                readSleepMinutes(client, range)
            } else {
                null
            },
            stepsToday = if (stepsPermission in granted) {
                readSteps(client, range)
            } else {
                null
            },
            restingHeartRate = if (heartRatePermission in granted) {
                readRestingHeartRateEstimate(client, range)
            } else {
                null
            },
            workoutSessions = if (exercisePermission in granted) {
                readExerciseSessionCount(client, range)
            } else {
                null
            },
            sourceFreshness = if (date == today()) {
                HealthSignalsFreshness.TODAY
            } else {
                HealthSignalsFreshness.STALE
            }
        )
    }

    private suspend fun readSleepMinutes(
        client: HealthConnectClient,
        range: TimeRangeFilter
    ): Int? =
        safeHealthRead {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = range
                )
            ).records
                .sumOf { record -> Duration.between(record.startTime, record.endTime).toMinutes() }
                .toInt()
                .takeIf { it > 0 }
        }

    private suspend fun readSteps(
        client: HealthConnectClient,
        range: TimeRangeFilter
    ): Int? =
        safeHealthRead {
            val result = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = range
                )
            )
            result[StepsRecord.COUNT_TOTAL]?.toInt()
        }

    private suspend fun readRestingHeartRateEstimate(
        client: HealthConnectClient,
        range: TimeRangeFilter
    ): Int? =
        safeHealthRead {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = range
                )
            ).records
                .flatMap { it.samples }
                .map { it.beatsPerMinute.toInt() }
                .minOrNull()
        }

    private suspend fun readExerciseSessionCount(
        client: HealthConnectClient,
        range: TimeRangeFilter
    ): Int? =
        safeHealthRead {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = range
                )
            ).records.size.takeIf { it > 0 }
        }

    private fun clientOrNull(): HealthConnectClient? =
        if (sdkAvailable()) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }

    private fun sdkAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private suspend fun <T> safeHealthRead(block: suspend () -> T): T? =
        try {
            block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        }

    private companion object {
        const val MAX_RECENT_DAYS = 14
    }
}
