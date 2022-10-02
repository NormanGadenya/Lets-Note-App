package com.neuralbit.letsnote.room.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.room.daos.ReminderDao
import com.neuralbit.letsnote.room.entities.Reminder

class ReminderRoomRepo (private val reminderDao: ReminderDao){

    suspend fun insert(reminder: Reminder){
        reminderDao.insert(reminder)
    }

    fun delete(noteUid: String){
        reminderDao.delete(noteUid)
    }

    fun fetchReminder(noteUid: String): LiveData<Reminder> {
        return reminderDao.getReminder(noteUid)
    }
}