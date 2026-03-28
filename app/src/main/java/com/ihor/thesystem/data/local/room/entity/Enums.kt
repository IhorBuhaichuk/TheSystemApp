package com.ihor.thesystem.data.local.room.entity

enum class QuestType   { DAILY, MAIN }
enum class QuestStatus { ACTIVE, COMPLETED, FAILED }

enum class WeightType {
    ABSOLUTE,    // Штанга/Гантелі (повна вага)
    BODY_WEIGHT, // Власна вага
    ADDED_WEIGHT // Власна вага + обтяження
}
