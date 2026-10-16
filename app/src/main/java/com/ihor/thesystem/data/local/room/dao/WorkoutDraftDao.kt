package com.ihor.thesystem.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ihor.thesystem.data.local.room.entity.WorkoutDraftEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutDraftSetEntity
import com.ihor.thesystem.data.local.room.relations.WorkoutDraftWithSets
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDraftDao {

    @Transaction
    @Query("SELECT * FROM workout_drafts ORDER BY updatedAtMillis DESC LIMIT 1")
    abstract fun observeActiveDraft(): Flow<WorkoutDraftWithSets?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertDraft(draft: WorkoutDraftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertSets(sets: List<WorkoutDraftSetEntity>)

    @Query("DELETE FROM workout_drafts WHERE draftId != :draftId")
    protected abstract suspend fun deleteOtherDrafts(draftId: String)

    @Query("DELETE FROM workout_drafts WHERE draftId = :draftId")
    abstract suspend fun discardDraft(draftId: String)

    @Transaction
    open suspend fun saveDraft(
        draft: WorkoutDraftEntity,
        sets: List<WorkoutDraftSetEntity>
    ) {
        deleteOtherDrafts(draft.draftId)
        insertDraft(draft)
        insertSets(sets)
    }
}

