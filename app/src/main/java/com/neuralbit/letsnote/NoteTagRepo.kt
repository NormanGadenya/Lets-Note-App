package com.neuralbit.letsnote

class NoteTagRepo(
    private val noteTagDao: NoteTagDao,

) {

    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef) {
        noteTagDao.insertNoteTagCrossRef(crossRef)
    }

    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef) {
        noteTagDao.deleteNoteTagCrossRef(crossRef)
    }

    suspend fun getNotesWithTag(tagTitle : String):List<NotesWithTag>{
        return noteTagDao.getNotesWithTag(tagTitle)
    }

    suspend fun getTagsWithNote(noteID: Long) :List<TagsWithNote>{
        return noteTagDao.getTagsWithNote(noteID)
    }

}

