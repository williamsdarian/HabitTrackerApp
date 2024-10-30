package com.example.habittrackerapp.MainActivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
}