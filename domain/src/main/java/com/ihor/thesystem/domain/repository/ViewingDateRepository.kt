package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.util.AppClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewingDateRepository @Inject constructor(
    private val clock: AppClock
) {
    private val _selectedDate = MutableStateFlow<LocalDate?>(today())
    val selectedDate = _selectedDate.asStateFlow()

    fun setDate(date: LocalDate?) {
        _selectedDate.value = date
    }

    fun selectToday() {
        _selectedDate.value = today()
    }

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
}
