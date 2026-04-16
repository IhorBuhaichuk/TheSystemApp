package com.ihor.thesystem.core.util

interface AppClock {
    fun now(): Long
}

class RealClock : AppClock {
    override fun now(): Long = System.currentTimeMillis()
}
