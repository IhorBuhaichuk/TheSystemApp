package com.ihor.thesystem.data.local.room.converters

import androidx.room.TypeConverter
import com.ihor.thesystem.data.local.room.entity.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter fun questTypeToString(v: QuestType?): String? = v?.name
    @TypeConverter fun stringToQuestType(v: String?): QuestType? = v?.let { QuestType.valueOf(it) }

    @TypeConverter fun questStatusToString(v: QuestStatus?): String? = v?.name
    @TypeConverter fun stringToQuestStatus(v: String?): QuestStatus? = v?.let { QuestStatus.valueOf(it) }

    @TypeConverter fun weightTypeToString(v: WeightType?): String? = v?.name
    @TypeConverter fun stringToWeightType(v: String?): WeightType? = v?.let { WeightType.valueOf(it) }

    @TypeConverter
    fun fromDoubleMap(value: Map<String, Double>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toDoubleMap(value: String?): Map<String, Double>? = value?.let { Json.decodeFromString(it) }

    @TypeConverter
    fun fromIntMap(value: Map<String, Int>?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toIntMap(value: String?): Map<String, Int>? = value?.let { Json.decodeFromString(it) }
}
