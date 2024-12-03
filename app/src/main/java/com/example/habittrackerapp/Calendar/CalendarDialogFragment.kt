package com.example.habittrackerapp.Calendar


import android.app.Dialog
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.mutableStateMapOf
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
import java.time.LocalDate
import java.time.YearMonth
import com.example.habittrackerapp.Calendar.ui.theme.DynamicCalendarTheme


@RequiresApi(Build.VERSION_CODES.O)
class CalendarDialogFragment(
) : DialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext()).apply {
            setContentView(ComposeView(requireContext()).apply {
                setContent {
                    DynamicCalendarTheme {
                        Scaffold(modifier = Modifier.padding(16.dp)) { innerPadding ->
                            DynamicCalendar(
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            })
        }
    }

    companion object {
        fun showDialog(fragmentManager: FragmentManager) {
            val dialogFragment = CalendarDialogFragment()
            dialogFragment.show(fragmentManager, "calendar_dialog")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    fun DynamicCalendar(modifier: Modifier = Modifier) {
        val completedDates = remember { mutableStateMapOf<LocalDate, Boolean>() }
        val today = LocalDate.now()
        val currentMonth = YearMonth.now()

        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeek =
            currentMonth.atDay(1).dayOfWeek.value % 7 // Adjust for zero-based index
        val totalCells = firstDayOfWeek + daysInMonth

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "${currentMonth.month} ${currentMonth.year}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7), // 7 columns for days of the week
                modifier = Modifier.weight(1f)
            ) {
                // Add empty cells for days before the first day of the month
                items(firstDayOfWeek) {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                // Add the actual days of the month
                items(daysInMonth) { day ->
                    val date = currentMonth.atDay(day + 1)
                    val isCompleted = completedDates[date] ?: false

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = if (isCompleted) Color.Green else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )

                    ) {
                        Text(
                            text = "${day + 1}",
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) Color.White else Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Completion Button
            Button(

                onClick = { completedDates[today] = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Mark Today Complete")
            }
        }
    }
}