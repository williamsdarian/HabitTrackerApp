package com.example.habittrackerapp.NewHabitActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.habittrackerapp.Repository.Habit
import com.example.habittrackerapp.Repository.HabitRepository


class NewHabitViewModel(private val repository: HabitRepository) : ViewModel() {
    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    val _habit = MutableLiveData<Habit>().apply{value=null}
    val word: LiveData<Habit>
        get() = _habit

    fun start(habitId:Int){
        viewModelScope.launch {
            repository.allHabits.collect{
                _habit.value = it[habitId]
            }
        }
    }

    fun insert(habit: Habit) = viewModelScope.launch {
        repository.insert(habit)
    }

    fun update(habit: Habit) = viewModelScope.launch {
        repository.update(habit)
    }

    fun delete(habit: Habit) = viewModelScope.launch {
        repository.deletHabitbyId(word)
    }
}

class NewHabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewHabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewHabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}