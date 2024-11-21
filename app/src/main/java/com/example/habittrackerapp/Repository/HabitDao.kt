package com.example.habittrackerapp.Repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao

interface HabitDao {
    @MapInfo(keyColumn = "id")
    @Query("SELECT * FROM habit_table ORDER BY title ASC")
    fun getAlphabetizedWords(): Flow<Map<Int,Habit>>

    @Update
    suspend fun update(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(habit: Habit)

    @Query("DELETE FROM habit_table")
    suspend fun deleteAll()

    @Query("DELETE FROM habit_table WHERE id = :id")
    suspend fun deleteHabitById(id: Int)
}
