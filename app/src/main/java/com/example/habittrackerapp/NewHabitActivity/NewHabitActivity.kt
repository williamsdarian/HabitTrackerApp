package com.example.habittrackerapp.NewHabitActivity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.habittrackerapp.R
import com.example.habittrackerapp.Repository.Habit
import com.example.habittrackerapp.HabitsApplication

class NewHabitActivity : AppCompatActivity() {

    private lateinit var editTitleView: EditText
    private lateinit var habit: Habit
    val newHabitViewModel: NewHabitViewModel by viewModels {
        NewHabitViewModelFactory((application as HabitsApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_habit)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        editTitleView = findViewById(R.id.edit_word)

        //Logic block to determine whether we are updating an exiting word
        //Or creating a new word
        //Get intent that created the activity id value, if exists
        val id = intent.getIntExtra("EXTRA_ID",-1)
        //If it doesn't exist, create a new Word object
        if(id == -1){
            habit = Habit(null,"", "")
        }else{
            //Otherwise, start the viewModel with the id
            //And begin observing the word to set the text in the
            //text view
            newHabitViewModel.start(id)
            newHabitViewModel.word.observe(this){
                if(it != null){
                    editTitleView.setText(it.title)
                }
            }
        }

        //Get reference to the button
        val button = findViewById<Button>(R.id.button_save)
        //Set the click listener functionality
        //If text is empty, return with nothing
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editTitleView.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                //If text isn't empty, determine whether to update
                //or insert
                val title = editTitleView.text.toString()
                if(newHabitViewModel.word.value?.id == null){
                    newHabitViewModel.insert(Habit(null, title, ""))
                }else{
                    newHabitViewModel.word.value?.let { it1 -> newHabitViewModel.update(it1) }
                }
                //replyIntent.putExtra(EXTRA_REPLY, word)
                setResult(Activity.RESULT_OK)
            }
            //End the activity
            finish()
        }

    }
}