package com.example.habittrackerapp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import android.app.Application
import com.example.habittrackerapp.Repository.HabitRepository
import com.example.habittrackerapp.Repository.HabitRoomDatabase


class HabitsApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { HabitRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { HabitRepository(database.habitDao()) }
}