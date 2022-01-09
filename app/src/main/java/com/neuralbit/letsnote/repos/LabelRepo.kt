package com.neuralbit.letsnote.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.daos.LabelDao
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.entities.Reminder
import com.neuralbit.letsnote.relationships.LabelWIthNotes

class LabelRepo(
    private val labelDao: LabelDao
) {
    suspend fun insert(label: Label){
        labelDao.insert(label)
    }

    suspend fun delete(noteID: Long){
        labelDao.delete(noteID)
    }

    fun getNotesWithLabel(labelID : Int): LiveData<List<LabelWIthNotes>> {
        return labelDao.getNotesWithLabel(  labelID )
    }
    fun getAllNotes(): LiveData<List<LabelWIthNotes>> {
        return labelDao.getAllNotes(   )
    }

    fun getNoteLabel(noteID : Long): LiveData<Label> {
        return labelDao.getNoteLabel(noteID)
    }

}