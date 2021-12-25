package com.neuralbit.letsnote.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.entities.Reminder

@Dao
interface ReminderDao {

    @Insert(onConflict =OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Transaction
    @Query("select * from Reminder where noteID = :noteID")
    fun getReminder(noteID :Long): LiveData<Reminder>
}