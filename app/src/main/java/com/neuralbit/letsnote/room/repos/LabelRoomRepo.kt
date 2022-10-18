package com.neuralbit.letsnote.room.repos

import com.neuralbit.letsnote.room.daos.LabelDao
import com.neuralbit.letsnote.room.entities.Label
import com.neuralbit.letsnote.room.relationships.LabelWIthNotes

class LabelRoomRepo(
    private val labelDao: LabelDao
) {
    suspend fun insert(label: Label){
        labelDao.insert(label)
    }

    suspend fun deleteLabel(labelColor: Int){
        labelDao.deleteLabel(labelColor)
    }


    suspend fun getNotesWithLabel(labelColor : Int): List<LabelWIthNotes> {
        return labelDao.getNotesWithLabel( labelColor )
    }
    suspend fun getAllLabels(): List<LabelWIthNotes> {
        return labelDao.getAllLabels()
    }

    suspend fun deleteAllLabels() {
        labelDao.deleteAllLabels()
    }

}