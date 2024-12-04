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

            // Add sample words.
            var word = Habit(null,"Hello", "Basic description", "", false, emptyList())
            habitDao.insert(word)
            word = Habit(null,"World!", "Lorem Ipsum Dolor...", "", false, emptyList())
            habitDao.insert(word)




        //when implenting the streaks in a ideal workflow the user will never be able to slkect a date further than the current day, thus the streak number can be determined by taking the total number of consecutive compelted dayas
        //for testing purposes we can jsut do it witha  counter adn have it preset ot have 1,2,3,4 of decmeber all filled out. and then we can have another task that breaks and just hard code whatevver we decide the streak to be
        }
    }
}