package com.neuralbit.letsnote.room.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.room.entities.Reminder

@Dao
interface ReminderDao {

    @Insert(onConflict =OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder)

    @Query("delete from Reminder where noteUid = :noteUid")
    fun delete(noteUid: String)

    @Transaction
    @Query("select * from Reminder where noteUid = :noteUid")
    fun getReminder(noteUid :String): LiveData<Reminder>
}