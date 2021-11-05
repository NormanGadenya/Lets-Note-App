package com.neuralbit.letsnote

import androidx.lifecycle.LiveData

class NoteRepo( private val notesDao : NotesDao ) {
    val allNotes: LiveData<List<Note>> = notesDao.getAllNotes()
    suspend fun  insert(note:Note){
        notesDao.insert(note)

    }


    suspend fun delete(note:Note){
        notesDao.delete(note)
    }

    suspend fun update(note: Note){
        notesDao.update(note)
    }
}