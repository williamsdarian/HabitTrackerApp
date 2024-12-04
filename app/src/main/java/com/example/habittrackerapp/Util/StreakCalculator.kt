package com.example.habittrackerapp.Util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StreakCalculator {

    companion object {
        fun calculateStreak(completedDates: List<String>): Int {
            if (completedDates.isEmpty()) return 0

            val formatter = DateTimeFormatter.ISO_DATE
            val sortedDates = completedDates.map { LocalDate.parse(it, formatter) }.sorted()

            var streak = 1
            var maxStreak = 1

            for (i in 1 until sortedDates.size) {
                val previous = sortedDates[i - 1]
                val current = sortedDates[i]
                if (current.minusDays(1) == previous) {
                    streak++
                    maxStreak = maxOf(maxStreak, streak)
                } else {
                    streak = 1
                }
            }

            return maxStreak
        }
    }


}