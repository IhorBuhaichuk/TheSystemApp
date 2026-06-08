package com.ihor.thesystem.domain.model

enum class EquipmentType {
    BODY_ONLY,
    DUMBBELL,
    BARBELL,
    MACHINE,
    CABLE,
    BANDS,
    KETTLEBELL,
    MEDICINE_BALL,
    EXERCISE_BALL,
    EZ_CURL_BAR,
    FOAM_ROLL,
    BENCH,
    PULL_UP_BAR,
    DIP_BARS,
    OTHER;

    companion object {
        fun fromRawEquipment(raw: String?): Set<EquipmentType> {
            val normalized = raw
                ?.trim()
                ?.lowercase()
                .orEmpty()

            if (normalized.isBlank()) return setOf(BODY_ONLY)

            val mapped = buildSet {
                if (normalized.contains("body") || normalized.contains("none")) add(BODY_ONLY)
                if (normalized.contains("dumbbell")) add(DUMBBELL)
                if (normalized.contains("barbell")) add(BARBELL)
                if (normalized.contains("machine") || normalized.contains("smith")) add(MACHINE)
                if (normalized.contains("cable")) add(CABLE)
                if (normalized.contains("band")) add(BANDS)
                if (normalized.contains("kettlebell")) add(KETTLEBELL)
                if (normalized.contains("medicine")) add(MEDICINE_BALL)
                if (normalized.contains("exercise ball") || normalized.contains("stability ball")) add(EXERCISE_BALL)
                if (normalized.contains("e-z") || normalized.contains("ez curl")) add(EZ_CURL_BAR)
                if (normalized.contains("foam")) add(FOAM_ROLL)
                if (normalized.contains("bench")) add(BENCH)
                if (normalized.contains("pull-up") || normalized.contains("pull up")) add(PULL_UP_BAR)
                if (normalized.contains("dip")) add(DIP_BARS)
                if (normalized.contains("other")) add(OTHER)
            }

            return mapped.ifEmpty { setOf(OTHER) }
        }
    }
}

data class EquipmentProfile(
    val trainsAtGym: Boolean = false,
    val availableEquipment: Set<EquipmentType> = setOf(EquipmentType.BODY_ONLY),
    val dumbbellMaxKg: Float? = null,
    val barbellAvailable: Boolean = false,
    val benchAvailable: Boolean = false,
    val pullUpBarAvailable: Boolean = false,
    val dipBarsAvailable: Boolean = false,
    val bandsAvailable: Boolean = false,
    val machinesAvailable: Boolean = false
) {
    val resolvedEquipment: Set<EquipmentType>
        get() = buildSet {
            add(EquipmentType.BODY_ONLY)
            addAll(availableEquipment)
            if (barbellAvailable) add(EquipmentType.BARBELL)
            if (benchAvailable) add(EquipmentType.BENCH)
            if (pullUpBarAvailable) add(EquipmentType.PULL_UP_BAR)
            if (dipBarsAvailable) add(EquipmentType.DIP_BARS)
            if (bandsAvailable) add(EquipmentType.BANDS)
            if (machinesAvailable) {
                add(EquipmentType.MACHINE)
                add(EquipmentType.CABLE)
            }
        }

    fun allows(exercise: ExerciseDetails): Boolean {
        val required = EquipmentType.fromRawEquipment(exercise.equipment)
        val available = resolvedEquipment
        val normalizedName = listOfNotNull(exercise.name, exercise.nameUk)
            .joinToString(separator = " ")
            .lowercase()

        if (requiresBench(normalizedName) && EquipmentType.BENCH !in available) return false
        if (requiresPullUpBar(normalizedName) && EquipmentType.PULL_UP_BAR !in available) return false
        if (requiresDipBars(normalizedName) && EquipmentType.DIP_BARS !in available) return false

        return required.all { type ->
            when (type) {
                EquipmentType.BODY_ONLY -> true
                EquipmentType.CABLE -> type in available || EquipmentType.MACHINE in available
                EquipmentType.OTHER -> trainsAtGym || type in available
                else -> type in available
            }
        }
    }

    private fun requiresBench(name: String): Boolean =
        name.contains("bench") || name.contains("press on bench")

    private fun requiresPullUpBar(name: String): Boolean =
        name.contains("pull-up") || name.contains("pull up") || name.contains("chin-up") || name.contains("chin up")

    private fun requiresDipBars(name: String): Boolean =
        name.contains("dip") || name.contains("parallel bar")
}
