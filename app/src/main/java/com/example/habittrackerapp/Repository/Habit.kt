package com.example.habittrackerapp.Repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_table")
data class Habit (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name="title") val title:String,
    @ColumnInfo(name="desc") val desc:String
)