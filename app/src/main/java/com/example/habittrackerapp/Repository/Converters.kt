package com.example.habittrackerapp.Repository

import android.util.Log
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.time.LocalDate


class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromList(value: List<String>): String {
        // Serialize as a flat JSON array, avoiding nested structures
        return try {
            Gson().toJson(value)
        } catch (e: Exception) {
            Log.e("Converters", "Failed to serialize completedDates: $value", e)
            "[]"
        }
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return try {
            // Deserialize JSON directly into a flat list
            val listType = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(value, listType)
        } catch (e: Exception) {
            Log.e("Converters", "Failed to deserialize completedDates: $value", e)
            emptyList()
        }
    }

    // Helper function to clean up entries
    private fun sanitizeEntry(entry: String): String {
        return entry.trim().removeSurrounding("[\"").removeSurrounding("\"]")
    }

}