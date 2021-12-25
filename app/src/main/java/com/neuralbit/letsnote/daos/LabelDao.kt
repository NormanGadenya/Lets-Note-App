package com.neuralbit.letsnote.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.relationships.LabelWIthNotes

@Dao
interface LabelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: Label)

    @Delete
    suspend fun delete(label: Label)

    @Transaction
    @Query("select * from Label where labelID = :labelID ")
    fun getNotesWithLabel(labelID :Int): LiveData<List<LabelWIthNotes>>


}