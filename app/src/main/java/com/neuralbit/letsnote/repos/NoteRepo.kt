package com.neuralbit.letsnote.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.daos.NotesDao
import com.neuralbit.letsnote.entities.*

class NoteRepo(
    private val notesDao : NotesDao
    ) {
    val notesWithoutPinArc: LiveData<List<Note>> = notesDao.getNotesWithoutPinArc()
    val allNotes: LiveData<List<Note>> = notesDao.getAllNotes()
    val archivedNotes : LiveData<List<Note>> = notesDao.getArchivedNotes()
    val deletedNotes : LiveData<List<Note>> = notesDao.getDeletedNotes()
    val pinnedNotes : LiveData<List<Note>> = notesDao.getPinnedNotes()
    suspend fun  insert(note: Note) : Long{
        return notesDao.insert(note)
    }


    suspend fun delete(note: Note){
        notesDao.delete(note)
    }

    suspend fun update(note: Note){
        notesDao.update(note)
    }

    suspend fun  insertTodo(todoItem: TodoItem) {
        return notesDao.insertTodo(todoItem)
    }


    suspend fun deleteTodo(todoItem: TodoItem){
        notesDao.deleteTodo(todoItem)
    }

    suspend fun updateTodo(todoItem: TodoItem){
        notesDao.updateTodo(todoItem)
    }

    suspend fun insertDeletedNote (noteID: DeletedNote){
        notesDao.insertDeleted(noteID)
    }
    suspend fun restoreDeletedNote (noteID: DeletedNote){
        notesDao.restoreDeleted(noteID)
    }

    fun getNote(noteID: Long) : LiveData<Note>{
        return notesDao.getNote(noteID)
    }
    fun getTodoList(noteID: Long) : LiveData<List<TodoItem>>{
        return notesDao.getTodoList(noteID)
    }

    fun getArchivedNote(noteID: Long) : LiveData<ArchivedNote>{
        return notesDao.getArchivedNote(noteID)
    }
    fun getDeletedNote(noteID: Long) : LiveData<DeletedNote>{
        return notesDao.getDeletedNote(noteID)
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