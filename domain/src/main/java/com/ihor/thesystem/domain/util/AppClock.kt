package com.ihor.thesystem.domain.util

import java.time.ZoneId

interface AppClock {
    fun now(): Long
    fun zoneId(): ZoneId = ZoneId.systemDefault()
}

class RealClock : AppClock {
    override fun now(): Long = System.currentTimeMillis()
    override fun zoneId(): ZoneId = ZoneId.systemDefault()
}
