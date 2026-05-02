package com.ihor.thesystem.domain.util

import java.time.LocalDate

data class ParsedAnnualProgressionPlanNote(
    val startDate: LocalDate,
    val adaptationEndDate: LocalDate?,
    val inventoryStep: Double?,
    val monthlyTargets: List<ParsedAnnualProgressionMonthlyTarget>
)

data class ParsedAnnualProgressionMonthlyTarget(
    val monthIndex: Int,
    val weight: Double,
    val adjustmentCode: String?
)

object AnnualProgressionPlanNoteParser {
    fun parse(note: String?): ParsedAnnualProgressionPlanNote? {
        if (note.isNullOrBlank() || !note.startsWith(ANNUAL_NOTE_PREFIX)) return null

        val parts = note.split("|")
        val startDate = parts.valueAfterPrefix("start=")
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return null
        val adaptationEndDate = parts.valueAfterPrefix("adaptationEnd=")
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        val inventoryStep = parts.valueAfterPrefix("step=")?.toDoubleOrNull()
        val targets = parts.lastOrNull()
            ?.split(";")
            .orEmpty()
            .mapNotNull { it.toMonthlyTargetOrNull() }
            .sortedBy { it.monthIndex }

        if (targets.isEmpty()) return null

        return ParsedAnnualProgressionPlanNote(
            startDate = startDate,
            adaptationEndDate = adaptationEndDate,
            inventoryStep = inventoryStep,
            monthlyTargets = targets
        )
    }

    private fun List<String>.valueAfterPrefix(prefix: String): String? =
        firstOrNull { it.startsWith(prefix) }?.removePrefix(prefix)

    private fun String.toMonthlyTargetOrNull(): ParsedAnnualProgressionMonthlyTarget? {
        val parts = split(":")
        val monthIndex = parts.getOrNull(0)
            ?.removePrefix("M")
            ?.toIntOrNull()
            ?: return null
        val weight = parts.getOrNull(1)?.toDoubleOrNull() ?: return null
        return ParsedAnnualProgressionMonthlyTarget(
            monthIndex = monthIndex,
            weight = weight,
            adjustmentCode = parts.getOrNull(2)
        )
    }
}

private const val ANNUAL_NOTE_PREFIX = "annual_step_loading"
