package com.neuralbit.letsnote.repos

import androidx.lifecycle.LiveData
import androidx.room.Query
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

    suspend fun deleteNoteLabel(noteID: Long){
        labelDao.deleteNoteLabel(noteID)
    }
    suspend fun deleteLabel(labelID: Int){
        labelDao.deleteLabel(labelID)
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