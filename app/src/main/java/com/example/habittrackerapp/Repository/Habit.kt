package com.example.habittrackerapp.Repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate

@Entity(tableName = "habit_table")
@TypeConverters(Converters::class) // Use the Converters for this DAO
data class Habit (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="title") val title:String,
    @ColumnInfo(name="desc") val desc:String,
    @ColumnInfo(name="date") val date:String,
    @ColumnInfo(name="isComplete") val isComplete:Boolean,
    @ColumnInfo(name="completedDates") val completedDates: List<String>
)