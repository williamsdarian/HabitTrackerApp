package com.example.habittrackerapp.NewHabitActivity

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
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
        editTitleView = findViewById(R.id.edit_task)
        editWordDetail = findViewById(R.id.edit_content)
        editTextDate = findViewById(R.id.edit_date)
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

        //Get reference to the button
        val saveButton = findViewById<Button>(R.id.button_save)
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
                if(newHabitViewModel.word.value?.id == null){
                    newHabitViewModel.insert(Habit(null, title, detail, date, isComplete))
                }else{
                    newHabitViewModel.word.value?.let { it1 -> newHabitViewModel.update(it1) }
                }

                // Set a notification for the next day at 9 AM
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
                calendar.set(Calendar.HOUR_OF_DAY, 9) // Set hour to 9 AM
                calendar.set(Calendar.MINUTE, 0)      // Set minute to 0
                calendar.set(Calendar.SECOND, 0)      // Set second to 0
                calendar.set(Calendar.MILLISECOND, 0) // Set millisecond to 0

                val nextDay9Am = calendar.timeInMillis

                // Schedule the reminder for 9 AM the next day
                setReminder(title, nextDay9Am)

                //replyIntent.putExtra(EXTRA_REPLY, word)
                setResult(Activity.RESULT_OK)
            }
            //End the activity
            finish()
        }

        // Delete a task and prompt the user to confirm deletion
        val deleteButton = findViewById<Button>(R.id.button_delete)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
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
        val shareButton = findViewById<Button>(R.id.share_habit)
        shareButton.setOnClickListener {
            val habitTitle = editTitleView.text.toString()
            val habitDetail = editWordDetail.text.toString()

            if ((!TextUtils.isEmpty(habitTitle)) && (!TextUtils.isEmpty(habitDetail))) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Task: $habitTitle\nDetails: $habitDetail")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share Task")
                startActivity(shareIntent)
            } else {
                Toast.makeText(this, "Habit information is missing!", Toast.LENGTH_SHORT).show()
            }
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
}