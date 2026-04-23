package com.ihor.thesystem.domain.model

enum class DomainQuestType { DAILY, MAIN, PROMOTION }
enum class DomainQuestStatus { ACTIVE, COMPLETED, FAILED, LOCKED }

enum class ExerciseCategory { STRENGTH, ENDURANCE, HYPERTROPHY, UNKNOWN }

enum class Rank(val weight: Int) {
    E(1), D(2), C(3), B(4), A(5), S(6);

    // Аліас для сумісності з існуючим кодом
    val value: Int get() = weight

    companion object {
        fun fromValue(value: Int): Rank = entries.find { it.weight == value } ?: E
    }
}

enum class PlayerRank(val title: String, val requiredMonths: Int) {
    NOVICE("Новачок", 1),
    APPRENTICE("Учень", 2),
    ADEPT("Адепт", 3),
    EXPERT("Експерт", 6),
    MASTER("Майстер", 9),
    THE_SYSTEM("Система", 12);

    companion object {
        fun resolveByMonth(month: Int): PlayerRank {
            return entries.sortedByDescending { it.requiredMonths }
                .firstOrNull { month >= it.requiredMonths } ?: NOVICE
        }
    }
}

enum class MuscleGroup(val label: String) {
    CHEST("Груди"),
    BACK("Спина"),
    SHOULDERS("Дельти"),
    QUADS("Квадрицепси"),
    HAMSTRINGS_GLUTES("Стегна та Сідниці"),
    ARMS("Руки")
}
