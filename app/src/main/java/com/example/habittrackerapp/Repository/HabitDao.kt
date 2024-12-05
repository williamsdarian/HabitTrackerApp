package com.example.habittrackerapp.Repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
@TypeConverters(Converters::class) // Use the Converters for this DAO
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


    //get habit by ID
    @Query("SELECT * FROM habit_table WHERE id = :habitId LIMIT 1")
    suspend fun getHabitById(habitId: Int): Habit?

    @Query("SELECT completedDates FROM habit_table WHERE id = :habitId")
    suspend fun getCompletedDates(habitId: Int): List<String>

    @Query("UPDATE habit_table SET completedDates = :completedDates WHERE id = :habitId")
    suspend fun updateCompletedDates(habitId: Int, completedDates: List<String>)

}
