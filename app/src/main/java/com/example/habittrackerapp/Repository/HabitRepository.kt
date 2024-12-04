package com.example.habittrackerapp.Repository

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

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

    @WorkerThread
    suspend fun getCompletedDates(habitId: Int): List<String> {
        return habitDao.getCompletedDates(habitId)
    }


}