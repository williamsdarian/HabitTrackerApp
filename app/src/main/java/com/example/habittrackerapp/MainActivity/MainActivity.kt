package com.example.habittrackerapp.MainActivity

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habittrackerapp.HabitsApplication
import com.example.habittrackerapp.NewHabitActivity.NewHabitActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.habittrackerapp.R

class MainActivity : AppCompatActivity() {

    //ViewModel object to communicate between Activity and repository
    private val habitViewModel: HabitViewModel by viewModels {
        WordViewModelFactory((application as HabitsApplication).repository)
    }
    /**
    Callback function passed through to RecyclerViewItems to launch
    A new activity based on id
    @param id id of the item that is clicked
     */
    fun launchNewHabitActivity(id:Int){
        val secondActivityIntent = Intent(this, NewHabitActivity::class.java)
        secondActivityIntent.putExtra("EXTRA_ID",id)
        this.startActivity(secondActivityIntent)
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    /**
    onCreate callback, handle setting up the application
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Get reference to recyclerView object
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        //Create adapter class, passing the launchNewWordActivity callback
        val adapter = HabitListAdapter(this::launchNewHabitActivity)
        //Set the adapter for the recyclerView to the adapter object
        recyclerView.adapter = adapter
        //Set the recyclerview layout to be a linearLayoutManager with activity context
        recyclerView.layoutManager = LinearLayoutManager(this)
        //Start observing the words list (now map), and pass updates through
        //to the adapter
        habitViewModel.allHabits.observe(this, Observer { habits ->
            // Update the cached copy of the words in the adapter.
            habits?.let { adapter.submitList(it.values.toList()) }
        })
        //Get reference to floating action button
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        //Start the NewWordActivity when it is clicked
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, NewHabitActivity::class.java)
            startActivity(intent)
        }
    }

    // Method to check and request necessary permissions on startup
    private fun checkAndRequestPermissions() {
        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestNotificationPermission()
        }

        // Request exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkAndRequestExactAlarmPermission()
        }
    }

    // Request notification permission for Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAndRequestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

            // Request the POST_NOTIFICATIONS permission
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Request exact alarm permission for Android 12+
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkAndRequestExactAlarmPermission() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            // Show permission dialog for exact alarms
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            Toast.makeText(this, "Enable Alarm Permissions to continue...", Toast.LENGTH_SHORT)
                .show()
            startActivity(intent)
        }
    }

    // Handle the result of permission requests
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}