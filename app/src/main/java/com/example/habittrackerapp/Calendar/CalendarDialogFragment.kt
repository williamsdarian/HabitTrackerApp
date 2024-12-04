package com.example.habittrackerapp.Calendar


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import java.time.LocalDate
import java.time.YearMonth
import com.example.habittrackerapp.Calendar.ui.theme.DynamicCalendarTheme
import com.example.habittrackerapp.NewHabitActivity.NewHabitActivity
import com.example.habittrackerapp.Repository.Habit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
class CalendarDialogFragment() : DialogFragment() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Extract arguments
        val completedDates = arguments?.getStringArrayList("completedDates")?.toList() ?: emptyList()
        Log.d("CalendarDialogFragment", "Loaded completedDates for dialog: $completedDates")
        val habitId = arguments?.getInt("habitId") ?: -1

        Log.d("CalendarDialogFragment", "Completed Dates: $completedDates")
        Log.d("CalendarDialogFragment", "Habit ID: $habitId")

        // Verify arguments
        if (habitId == -1) {
            Log.e("CalendarDialogFragment", "Invalid habit ID, dialog not created.")
            return super.onCreateDialog(savedInstanceState)
        }

        return Dialog(requireContext()).apply {
            setContentView(ComposeView(requireContext()).apply {
                setContent {
                    DynamicCalendarTheme {
                        Scaffold(modifier = Modifier.padding(16.dp)) { innerPadding ->
                            Log.d("CalendarDialogFragment", "Completed Dates passed to DynamicCalendar: $completedDates")
                            DynamicCalendar(
                                modifier = Modifier.padding(innerPadding),
                                completedDates = completedDates,
                                onMarkComplete = { date ->
                                    Log.d("CalendarDialogFragment", "Marking date as complete: $date")
                                    // Update completed dates
                                    // Ensure parent activity updates storage as needed
                                    (requireActivity() as? NewHabitActivity)?.newHabitViewModel
                                        ?.updateCompletedDates(habitId, date.toString())
                                },
                                onClose = { dismiss() } // Close the dialog
                            )
                        }
                    }
                }
            })
        }
    }
    

    companion object {
        fun showDialog(fragmentManager: FragmentManager, completedDates: List<String>, habitId: Int?) {
            val dialogFragment = CalendarDialogFragment()
            val args = Bundle().apply {
                putStringArrayList("completedDates", ArrayList(completedDates))
                habitId?.let { putInt("habitId", it) }
            }
            dialogFragment.arguments = args
            dialogFragment.show(fragmentManager, "calendar_dialog")
        }
    }

    @SuppressLint("RememberReturnType", "MutableCollectionMutableState")
    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun DynamicCalendar(
        modifier: Modifier = Modifier,
        completedDates: List<String>,
        onMarkComplete: (LocalDate) -> Unit,
        onClose: () -> Unit
    ) {
        val today = LocalDate.now()
        //val currentMonth = YearMonth.now()
        val currentMonth = remember {mutableStateOf(YearMonth.now())}

//        val daysInMonth = currentMonth.lengthOfMonth()
//        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7

        // Track completed dates
        val markedDates = remember { mutableStateOf(setOf<LocalDate>()) }

        LaunchedEffect(completedDates) {
            Log.d("DynamicCalendar", "Raw completedDates: $completedDates")

            val validDates = completedDates.flatMap { entry ->
                // helps get rid of extra charcters liekly from nested JSON Strings
                try {
                    if (entry.startsWith("[\"") && entry.endsWith("\"]")) {
                        val innerList = Gson().fromJson<List<String>>(entry, object : TypeToken<List<String>>() {}.type)
                        innerList
                    } else {
                        listOf(entry)
                    }
                } catch (e: Exception) {
                    Log.e("DynamicCalendar", "Error parsing entry: $entry", e)
                    listOf<String>()
                }
            }.mapNotNull { dateStr ->
                try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    Log.e("DynamicCalendar", "Invalid date format: $dateStr", e)
                    null
                }
            }.toSet()

            Log.d("DynamicCalendar", "Valid Dates: $validDates")
            markedDates.value = validDates
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header displaying the current month
            Text(
                text = "${currentMonth.value.month} ${currentMonth.value.year}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            CalendarGrid(
                currentMonth = currentMonth.value,
                markedDates = markedDates.value,
                onMarkComplete = onMarkComplete
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Previous and Next buttons in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { currentMonth.value = currentMonth.value.minusMonths(1) }) {
                    Text(text = "Previous")
                }

                Button(onClick = { currentMonth.value = currentMonth.value.plusMonths(1) }) {
                    Text(text = "Next")
                }
            }

            // Mark Today Complete and Close buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (today !in markedDates.value) {
                            onMarkComplete(today)
                            markedDates.value = markedDates.value + today
                        }
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(text = "Mark Today Complete")
                }

                Button(onClick = onClose, modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = "Close")
                }
        }
    }

}

    @Composable
    fun CalendarGrid(currentMonth: YearMonth?, markedDates: Set<LocalDate>, onMarkComplete: (LocalDate) -> Unit) {
        val today = LocalDate.now()
        val daysInMonth = currentMonth?.lengthOfMonth()
        val firstDayOfWeek = currentMonth!!.atDay(1).dayOfWeek.value % 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Add empty cells for days before the first day of the month
            items(firstDayOfWeek) {
                Spacer(modifier = Modifier.size(40.dp)) // Empty space
            }

            // Add the actual days of the month
            if (daysInMonth != null) {
                items(daysInMonth) { day ->
                    val date = currentMonth.atDay(day + 1)
                    val isCompleted = date in markedDates
                    val isToday = date == today

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = when {
                                    isCompleted -> Color.Green
                                    isToday -> Color.Blue
                                    else -> Color.LightGray
                                },
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${day + 1}",
                            color = if (isCompleted || isToday) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

    }
}

