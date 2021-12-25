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

    suspend fun getReminder(noteID : Long): Reminder {
        return reminderDao.getReminder(noteID)
    }
}