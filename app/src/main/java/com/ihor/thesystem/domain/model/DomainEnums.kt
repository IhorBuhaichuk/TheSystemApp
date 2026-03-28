package com.ihor.thesystem.domain.model

enum class DomainQuestType { DAILY, MAIN }
enum class DomainQuestStatus { ACTIVE, COMPLETED, FAILED }

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
