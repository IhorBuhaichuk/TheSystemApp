package com.ihor.thesystem.domain.model

enum class DomainQuestType { DAILY, MAIN }
enum class DomainQuestStatus { ACTIVE, COMPLETED, FAILED }

enum class PlayerRank(val title: String) {
    RANK_1("Новачок"),
    RANK_2("УЧЕНЬ"),
    RANK_3("ПОСЛІДОВНИК"),
    RANK_4("ВОЇН"),
    RANK_5("ВЕТЕРАН"),
    RANK_6("МАЙСТЕР"),
    RANK_7("ЕЛІТНИЙ ВОЇН"),
    RANK_8("ЧЕМПІОН"),
    RANK_9("ЛЕГЕНДА"),
    RANK_10("БЕЗСМЕРТНИЙ"),
    RANK_11("НАПІВБОГ"),
    RANK_12("СИСТЕМА");

    companion object {
        fun fromMonth(month: Int): PlayerRank {
            return entries.getOrNull(month - 1) ?: RANK_12
        }
    }
}
