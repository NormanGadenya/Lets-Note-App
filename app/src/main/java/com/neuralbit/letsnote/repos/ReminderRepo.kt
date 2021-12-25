package com.neuralbit.letsnote.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.daos.ReminderDao
import com.neuralbit.letsnote.entities.Reminder

class ReminderRepo (private val reminderDao: ReminderDao){

    suspend fun insert(reminder: Reminder){
        reminderDao.insert(reminder)
    }

    suspend fun delete(reminder: Reminder){
        reminderDao.delete(reminder)
    }

    fun getReminder(noteID : Long): LiveData<Reminder> {
        return reminderDao.getReminder(noteID)
    }
}