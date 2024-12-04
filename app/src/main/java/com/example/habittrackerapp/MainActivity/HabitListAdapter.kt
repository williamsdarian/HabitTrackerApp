package com.example.habittrackerapp.MainActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.example.habittrackerapp.Util.StreakCalculator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habittrackerapp.Repository.Habit
import com.example.habittrackerapp.R



/**
WordListAdapter class
Implements a ListAdapter holding Words in WordViewHolders
Compares words with the WordsComparator
@param onItemClicked the callback function when an itemView is clicked
 */
class HabitListAdapter(
    val onItemClicked:(id:Int)->Unit)
    : ListAdapter<Habit, HabitListAdapter.HabitViewHolder>(HabitsComparator()) {

    /**
     * onCreateViewHolder creates the viewHolder object
     * Implements WordViewHolder
     * @param parent the object that holds the ViewGroup
     * @param viewType the type of the view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        return HabitViewHolder.create(parent)
    }

    /**
     * onBindViewHolder is called when a view object is bound to a view holder
     * @param holder the ViewHolder being created
     * @param position integer value for the position in the recyclerView
     */
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        //Get the item in a position
        val current = getItem(position)
        val streak = StreakCalculator.calculateStreak(current.completedDates) // Calculate streak
        //Set its onClickListener to the class callback parameter
        holder.itemView.setOnClickListener {
            current.id?.let { it1 -> onItemClicked(it1) }
        }
        //Bind the item to the holder
        holder.bind(current, streak)
    }

    /**
     * WordViewHolder class implements a RecyclerView.ViewHolder object
     * Responsible for creating the layouts and binding objects to views
     * @param itemView the View object to be bound
     */
    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //Reference to the textView object
        private val wordItemView: TextView = itemView.findViewById(R.id.textView)
        private val streakView: TextView = itemView.findViewById(R.id.streakView) // Add this for the streak


        /**
         * bind binds a word object's data to views
         */
        @SuppressLint("SetTextI18n")
        fun bind(habit: Habit?, streak: Int) {
            if (habit != null) {
                wordItemView.text = habit.title
                streakView.text = "Streak: $streak" // Display the streak

            }
        }

        /**
         * create the view object
         */
        companion object {
            fun create(parent: ViewGroup): HabitViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return HabitViewHolder(view)
            }
        }
    }

    /**
     * Comparators to determine whether to actually inflate new views
     */
    class HabitsComparator : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.title == newItem.title
        }
    }
}