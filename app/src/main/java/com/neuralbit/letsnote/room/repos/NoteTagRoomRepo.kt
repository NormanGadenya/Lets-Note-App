package com.neuralbit.letsnote.room.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.room.daos.NoteTagDao
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.relationships.NotesWithTag
import com.neuralbit.letsnote.room.relationships.TagsWithNote

class NoteTagRoomRepo(
    private val noteTagDao: NoteTagDao,

    ) {

    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef) {
        noteTagDao.insertNoteTagCrossRef(crossRef)
    }

    fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef) {
        noteTagDao.deleteNoteTagCrossRef(crossRef)
    }

    suspend fun getNotesWithTag(tagTitle : String):List<NotesWithTag>{
        return noteTagDao.getNotesWithTag(tagTitle)
    }

    fun getTagsWithNote(noteUid: String) :LiveData<List<TagsWithNote>>{
        return noteTagDao.getTagsWithNote(noteUid)
    }

}

