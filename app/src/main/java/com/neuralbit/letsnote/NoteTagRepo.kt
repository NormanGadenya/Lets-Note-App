package com.neuralbit.letsnote

import androidx.lifecycle.LiveData

class NoteTagRepo(
    private val noteTagDao: NoteTagDao,

) {

    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef) {
        noteTagDao.insertNoteTagCrossRef(crossRef)
    }

    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef) {
        noteTagDao.deleteNoteTagCrossRef(crossRef)
    }

    suspend fun getNotesWithTag(tagID : Int) {
        noteTagDao.getNotesWithTag(tagID)
    }

    suspend fun getTagsWithNote(noteID : Int){
        noteTagDao.getTagsWithNote(noteID)
    }

}

