package com.neuralbit.letsnote

import androidx.lifecycle.LiveData

class NoteRepo( private val notesDao : NotesDao ) {
    val allNotes: LiveData<List<Note>> = notesDao.getAllNotes()
    val archivedNotes : LiveData<List<Note>> = notesDao.getArchivedNotes()
    val pinnedNotes : LiveData<List<Note>> = notesDao.getPinnedNotes()
    suspend fun  insert(note:Note){
        notesDao.insert(note)
    }


    suspend fun delete(note:Note){
        notesDao.delete(note)
    }

    suspend fun update(note: Note){
        notesDao.update(note)
    }

    suspend fun insertArchive(id: ArchivedNote){
        notesDao.insertArchive(id)
    }
    suspend fun deleteArchive(id: ArchivedNote){
        notesDao.deleteArchive(id)
    }
    suspend fun insertPinned(id: PinnedNote){
        notesDao.insertPinned(id)
    }
    suspend fun deletePinned(id: PinnedNote){
        notesDao.deletePinned(id)
    }
}