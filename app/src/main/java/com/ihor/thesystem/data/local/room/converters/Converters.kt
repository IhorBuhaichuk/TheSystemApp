package com.ihor.thesystem.data.local.room.converters

import androidx.room.TypeConverter
import com.ihor.thesystem.data.local.room.entity.*
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.Rank
import com.ihor.thesystem.domain.model.ExerciseCategory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter fun questTypeToString(v: QuestType?): String? = v?.name
    @TypeConverter fun stringToQuestType(v: String?): QuestType? = v?.let { QuestType.valueOf(it) }

    @TypeConverter fun questStatusToString(v: QuestStatus?): String? = v?.name
    @TypeConverter fun stringToQuestStatus(v: String?): QuestStatus? = v?.let { QuestStatus.valueOf(it) }

    @TypeConverter fun weightTypeToString(v: WeightType?): String? = v?.name
    @TypeConverter fun stringToWeightType(v: String?): WeightType? = v?.let { WeightType.valueOf(it) }

    @TypeConverter fun rankToString(v: Rank?): String? = v?.name
    @TypeConverter fun stringToRank(v: String?): Rank? = v?.let { Rank.valueOf(it) }

    @TypeConverter fun exerciseCategoryToString(v: ExerciseCategory?): String? = v?.name
    @TypeConverter fun stringToExerciseCategory(v: String?): ExerciseCategory? = v?.let { ExerciseCategory.valueOf(it) }

    @TypeConverter fun playerRankToString(v: PlayerRank?): String? = v?.name
    @TypeConverter fun stringToPlayerRank(v: String?): PlayerRank? = v?.let {
        try {
            PlayerRank.valueOf(it)
        } catch (e: IllegalArgumentException) {
            when(it) {
                "Новачок" -> PlayerRank.NOVICE
                "Учень" -> PlayerRank.APPRENTICE
                "Адепт" -> PlayerRank.ADEPT
                "Експерт" -> PlayerRank.EXPERT
                "Майстер" -> PlayerRank.MASTER
                "Система" -> PlayerRank.THE_SYSTEM
                else -> PlayerRank.NOVICE
            }
        }
    }

    @TypeConverter
    fun fromDoubleMap(value: Map<String, Double>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toDoubleMap(value: String?): Map<String, Double>? = value?.let { Json.decodeFromString(it) }

    @TypeConverter
    fun fromIntMap(value: Map<String, Int>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toIntMap(value: String?): Map<String, Int>? = value?.let { Json.decodeFromString(it) }

    @TypeConverter
    fun fromMuscleGroupList(value: List<com.ihor.thesystem.domain.model.MuscleGroup>?): String? {
        return value?.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toMuscleGroupList(value: String?): List<com.ihor.thesystem.domain.model.MuscleGroup>? {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull {
            try { com.ihor.thesystem.domain.model.MuscleGroup.valueOf(it) } catch (e: Exception) { null }
        }
    }
}
