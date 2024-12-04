package com.example.habittrackerapp.Repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class HabitRepository(private val habitDao: HabitDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allHabits: Flow<Map<Int,Habit>> = habitDao.getAlphabetizedWords()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(habit: Habit) {
        habitDao.insert(habit)
    }

    @WorkerThread
    suspend fun update(habit: Habit){
        habitDao.update(habit)
    }

    @WorkerThread
    suspend fun deleteHabitById(habit: Habit) {
        habit.id?.let { habitDao.deleteHabitById(it) }
    }


    suspend fun getHabitById(habitID: Int){
        habitDao.getHabitById(habitID)
    }



    @WorkerThread
    suspend fun getCompletedDates(habitId: Int): List<String> {
        val rawDates = habitDao.getCompletedDates(habitId)
        Log.d("RawData", "Raw completedDates from DB: $rawDates for habitId: $habitId")
//        val cleanedDates = rawDates.filterNot { it.isEmpty() || it.startsWith("[") } // Remove invalid entries
//        Log.d("GetDates", "Loaded cleaned completedDates: $cleanedDates for habitId: $habitId")
        return rawDates
    }

    @WorkerThread
    suspend fun updateCompletedDates(habitId: Int, completedDates: List<String>) {
        val sanitizedDates = completedDates.flatMap { entry ->
            try {
                if (entry.startsWith("[\"") && entry.endsWith("\"]")) {
                    Gson().fromJson<List<String>>(entry, object : TypeToken<List<String>>() {}.type)
                } else {
                    listOf(entry)
                }
            } catch (e: Exception) {
                Log.e("Repository", "Error sanitizing entry: $entry", e)
                listOf<String>()
            }
        }

        Log.d("Repository", "Sanitized Dates Before Saving: $sanitizedDates")
        habitDao.updateCompletedDates(habitId, sanitizedDates)

        // Verify what is being saved to the database
        val savedDates = habitDao.getCompletedDates(habitId)
        Log.d("Repository", "habitID = $habitId")
        Log.d("Repository", "Saved Dates After Update: $savedDates")
    }


    // Helper function for sanitizing entries
    private fun sanitizeEntry(entry: String): String {
        return entry.trim().removeSurrounding("[\"").removeSurrounding("\"]")
    }

}