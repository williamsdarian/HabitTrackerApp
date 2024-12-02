package com.example.habittrackerapp.NewHabitActivity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.habittrackerapp.R
import com.example.habittrackerapp.Repository.Habit
import com.example.habittrackerapp.HabitsApplication
import com.example.habittrackerapp.MainActivity.HabitReminderReceiver

class NewHabitActivity : AppCompatActivity() {

    private lateinit var editTitleView: EditText
    private lateinit var habit: Habit
    private lateinit var editWordDetail: EditText
    private lateinit var editTextDate: EditText
    private lateinit var checkBox: CheckBox
    private var dueDate: Long = 0L
    private var isComplete: Boolean = false
    val newHabitViewModel: NewHabitViewModel by viewModels {
        NewHabitViewModelFactory((application as HabitsApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_habit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editTitleView = findViewById(R.id.edit_habit_name)
        editWordDetail = findViewById(R.id.edit_content)
        editTextDate = findViewById(R.id.edit_reminder)
        checkBox = findViewById(R.id.task_checkbox)

        //Logic block to determine whether we are updating an exiting word
        //Or creating a new word
        //Get intent that created the activity id value, if exists
        val id = intent.getIntExtra("EXTRA_ID",-1)
        //If it doesn't exist, create a new Word object
        if(id == -1){
            habit = Habit(null,"", "", "", false)
        }else{
            //Otherwise, start the viewModel with the id
            //And begin observing the word to set the text in the
            //text view
            newHabitViewModel.start(id)
            newHabitViewModel.word.observe(this){
                if(it != null){
                    editTitleView.setText(it.title)
                    editWordDetail.setText(it.desc)
                    editTextDate.setText((it.date))
                    checkBox.isChecked = it.isComplete
                }
            }
        }

        editTextDate.setOnClickListener{
            showTimePickerDialog()
        }

        //Get reference to the button
        val saveButton = findViewById<Button>(R.id.button_save_habit)
        //Set the click listener functionality
        //If text is empty, return with nothing
        saveButton.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editTitleView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
                Toast.makeText(this, "No title, habit not saved!", Toast.LENGTH_SHORT).show()
            } else {
                //If text isn't empty, determine whether to update
                //or insert
                val title = editTitleView.text.toString()
                val detail = editWordDetail.text.toString()
                val date = editTextDate.text.toString()
                if (id > -1) {
                    newHabitViewModel.update(Habit(id, title, detail, date, isComplete))
                } else {
                    newHabitViewModel.insert(Habit(null, title, detail, date, isComplete))
                }

                // Schedule the reminder if the due date is in the future
                if (dueDate > System.currentTimeMillis()) {
                    setReminder(title, dueDate)
                }
                // IF the user did not set a time reminder, then set one for 9am next day
                else if (dueDate == 0L || editWordDetail.text.toString() == "")
                {
                    // Set a notification for the next day at 9 AM
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 9)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val nextDay9Am = calendar.timeInMillis
                    setReminder(title, nextDay9Am)

                }

                setResult(Activity.RESULT_OK)
            }
            //End the activity
            finish()
        }

        // Delete a task and prompt the user to confirm deletion
        val deleteButton = findViewById<Button>(R.id.button_cancel_habit)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Yes") { dialog, which ->
                    if(id != -1) {
                        newHabitViewModel.delete(Habit(id, editTitleView.text.toString(), editWordDetail.text.toString(),editTextDate.text.toString(), isComplete))
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Get task data and share as text
        val chooseIconButton = findViewById<Button>(R.id.icon_selector)
        chooseIconButton.setOnClickListener {
            // **TO-DO** Add functionality for this
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setReminder(taskName: String, timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, HabitReminderReceiver::class.java).apply {
            putExtra("taskName", taskName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            Toast.makeText(this, "Reminder set for $taskName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Cannot set exact alarms without permission", Toast.LENGTH_SHORT).show()
        }
    }

    // Show the time
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)

                displaySelectedDateTime(calendar)
            },
            hour, minute, false
        )
        timePickerDialog.show()
    }
    // Update the EditText to display both the selected date and time
    // store time in milliseconds
    @SuppressLint("SetTextI18n")
    private fun displaySelectedDateTime(calendar: Calendar) {
        var am_pm = ""
        if(calendar.get(Calendar.AM_PM) == 0) {
            am_pm = "AM"
        }
        else {
            am_pm = "PM"
        }

        val selectedTime = "${calendar.get(Calendar.HOUR)}:${String.format("%02d", calendar.get(Calendar.MINUTE))} ${am_pm}"

        editTextDate.setText(selectedTime)
        dueDate = calendar.timeInMillis
    }
}