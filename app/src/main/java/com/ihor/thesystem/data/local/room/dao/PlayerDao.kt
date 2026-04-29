package com.ihor.thesystem.data.local.room.dao

import androidx.room.*
import com.ihor.thesystem.data.local.room.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player WHERE id = 1")
    fun getPlayer(): Flow<PlayerEntity?>

    @Query("SELECT * FROM player WHERE id = 1 LIMIT 1")
    suspend fun getPlayerSync(): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(player: PlayerEntity)

    @Update
    suspend fun update(player: PlayerEntity)

    @Query("UPDATE player SET height = :height WHERE id = 1")
    suspend fun updateHeight(height: Float)

    @Query("UPDATE player SET age = :age WHERE id = 1")
    suspend fun updateAge(age: Int)

    @Query("UPDATE player SET currentCycleDay = :day WHERE id = 1")
    suspend fun updateCurrentCycleDay(day: Int)
}
