package com.ihor.thesystem.data.local.room.entity

enum class QuestType   { DAILY, MAIN, PROMOTION }
enum class QuestStatus { ACTIVE, COMPLETED, FAILED }

enum class WeightType {
    ABSOLUTE,    // Штанга/Гантелі (повна вага)
    BODY_WEIGHT, // Власна вага
    ADDED_WEIGHT // Власна вага + обтяження
}

enum class TaskCategory {
    NUTRITION,
    ACTIVITY,
    RECOVERY,
    SYSTEM
}

enum class ContextRequirement {
    NONE,
    AFTER_WAKE_UP,
    BEFORE_WORKOUT,
    AFTER_WORKOUT,
    BEFORE_SLEEP,
    DURING_DAY
}
