package com.neuralbit.letsnote.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.relationships.LabelWIthNotes

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: Label)

    @Query("delete from Label where noteID = :noteID")
    suspend fun deleteNoteLabel(noteID: Long)

    @Query("delete from Label where labelID = :labelID")
    suspend fun deleteLabel(labelID: Int)


    @Transaction
    @Query("select * from Label")
    fun getAllNotes () : LiveData<List<LabelWIthNotes>>

    @Transaction
    @Query("select * from Label where labelID = :labelID ")
    fun getNotesWithLabel(labelID :Int): LiveData<List<LabelWIthNotes>>

    @Transaction
    @Query("select * from Label where noteID = :noteID ")
    fun getNoteLabel(noteID :Long): LiveData<Label>


}