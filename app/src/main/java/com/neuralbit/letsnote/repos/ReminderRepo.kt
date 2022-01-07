package com.neuralbit.letsnote.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.daos.ReminderDao
import com.neuralbit.letsnote.entities.Reminder

class ReminderRepo (private val reminderDao: ReminderDao){

    suspend fun insert(reminder: Reminder){
        reminderDao.insert(reminder)
    }

    suspend fun delete(noteID: Long){
        reminderDao.delete(noteID)
    }

    fun fetchReminder(noteID : Long): LiveData<Reminder> {
        return reminderDao.getReminder(noteID)
    }
}