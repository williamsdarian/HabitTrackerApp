package com.example.habittrackerapp.Util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StreakCalculator {

    companion object {
        fun calculateStreak(completedDates: List<String>): Int {

            if (completedDates.isEmpty()) return 0

            val formatter = DateTimeFormatter.ISO_DATE
            val sortedDates = completedDates.map { LocalDate.parse(it, formatter) }.sorted()

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            // Check if yesterday was completed
            val lastCompletion = sortedDates.lastOrNull()
            if (lastCompletion != null && (lastCompletion.isEqual(today) || lastCompletion.isEqual(
                    yesterday
                ))
            ) {
                var streak = 1 // Start streak with yesterday or today as last completion
                for (i in sortedDates.size - 2 downTo 0) {
                    val previous = sortedDates[i]
                    if (lastCompletion.minusDays(streak.toLong()).isEqual(previous)) {
                        streak++ // Increment streak if consecutive
                    } else {
                        break // Stop counting if there's a gap
                    }
                }
                return streak
            }

            // If neither today nor yesterday is completed, reset streak to 0
            return 0
        }
    }
}