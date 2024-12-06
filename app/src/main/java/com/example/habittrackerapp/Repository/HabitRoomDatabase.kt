package com.example.habittrackerapp.Repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Word class
@Database(entities = arrayOf(Habit::class), version = 2, exportSchema = false)
@TypeConverters(Converters::class)
public abstract class HabitRoomDatabase: RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HabitRoomDatabase? = null

        fun getDatabase(context: Context, scope:CoroutineScope): HabitRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitRoomDatabase::class.java,
                    "habit_database"
                ).addCallback(HabitDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
    private class HabitDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.habitDao())
                }
            }
        }

        suspend fun populateDatabase(habitDao: HabitDao) {
            // Delete all content here.
            habitDao.deleteAll()

            val dates = listOf<String>("2024-11-02", "2024-11-03", "2024-11-04", "2024-11-05", "2024-11-06", "2024-11-15", "2024-11-16", "2024-11-17", "2024-11-18", "2024-11-19", "2024-11-20", "2024-11-21",
                "2024-11-22", "2024-11-23", "2024-11-24", "2024-11-25", "2024-11-26",
                "2024-12-01", "2024-12-02", "2024-12-03", "2024-12-04")
            val dates2 = listOf<String>("2024-11-01", "2024-11-02", "2024-11-03", "2024-11-04", "2024-11-09", "2024-11-10", "2024-11-11",
                "2024-11-12", "2024-11-13", "2024-11-14", "2024-11-15", "2024-11-16", "2024-11-20", "2024-11-21", "2024-11-22",
                "2024-11-28","2024-11-29","2024-11-30", "2024-12-01", "2024-12-02", "2024-12-03", "2024-12-04")
            // Add sample words.
            var word = Habit(null,"Explore new music", "Listen to a new album every day", "", false, dates)
            habitDao.insert(word)
            word = Habit(null,"Meditate", "Once a day ", "", false, dates2)
            habitDao.insert(word)

        }
    }
}