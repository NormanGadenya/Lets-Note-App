package com.neuralbit.letsnote.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.entities.ArchivedNote
import com.neuralbit.letsnote.daos.NotesDao
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.PinnedNote

class NoteRepo(
    private val notesDao : NotesDao
    ) {
    val notesWithoutPinArc: LiveData<List<Note>> = notesDao.getNotesWithoutPinArc()
    val allNotes: LiveData<List<Note>> = notesDao.getAllNotes()
    val archivedNotes : LiveData<List<Note>> = notesDao.getArchivedNotes()
    val pinnedNotes : LiveData<List<Note>> = notesDao.getPinnedNotes()
    suspend fun  insert(note: Note){
        notesDao.insert(note)
    }


    suspend fun delete(note: Note){
        notesDao.delete(note)
    }

    suspend fun update(note: Note){
        notesDao.update(note)
    }

    fun getNote(noteID: Long) : LiveData<Note>{
        return notesDao.getNote(noteID)
    }

    fun getArchivedNote(noteID: Long) : LiveData<ArchivedNote>{
        return notesDao.getArchivedNote(noteID)
    }

    fun getPinnedNote(noteID: Long) : LiveData<PinnedNote>{
        return notesDao.getPinnedNote(noteID)
    }

    suspend fun insertArchive(archivedNote: ArchivedNote){
        notesDao.insertArchive(archivedNote)
    }
    suspend fun deleteArchive(archivedNote: ArchivedNote){
        notesDao.deleteArchive(archivedNote)
    }
    suspend fun insertPinned(pinnedNote: PinnedNote){
        notesDao.insertPinned(pinnedNote)
    }
    suspend fun deletePinned(pinnedNote: PinnedNote){
        notesDao.deletePinned(pinnedNote)
    }
}