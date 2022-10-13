package com.neuralbit.letsnote.room.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.room.entities.Label
import com.neuralbit.letsnote.room.relationships.LabelWIthNotes

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: Label)


    @Query("delete from Label where labelColor = :labelColor")
    suspend fun deleteLabel(labelColor: Int)

    @Transaction
    @Query("select * from Label")
    fun getAllLabels () : LiveData<List<LabelWIthNotes>>

    @Transaction
    @Query("select * from Label where labelColor = :labelColor ")
    suspend fun getNotesWithLabel(labelColor :Int): List<LabelWIthNotes>

//    @Transaction
//    @Query("select * from Label where noteUid = :noteUid ")
//    fun getNoteLabel(noteUid :String): LiveData<Label>

}