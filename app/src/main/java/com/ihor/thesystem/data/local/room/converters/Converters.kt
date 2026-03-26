package com.ihor.thesystem.data.local.room.converters

import androidx.room.TypeConverter
import com.ihor.thesystem.data.local.room.entity.QuestStatus
import com.ihor.thesystem.data.local.room.entity.QuestType

class Converters {

    @TypeConverter fun questTypeToString(v: QuestType?): String? = v?.name
    @TypeConverter fun stringToQuestType(v: String?): QuestType? = v?.let { QuestType.valueOf(it) }

    @TypeConverter fun questStatusToString(v: QuestStatus?): String? = v?.name
    @TypeConverter fun stringToQuestStatus(v: String?): QuestStatus? = v?.let { QuestStatus.valueOf(it) }
}