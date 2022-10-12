package com.neuralbit.letsnote.room.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.room.daos.NotesDao
import com.neuralbit.letsnote.room.entities.*

class NoteRoomRepo(
    private val notesDao : NotesDao
    ) {
    suspend fun  insert(note: Note){
        return notesDao.insert(note)
    }


    fun delete(noteUid: String){
        notesDao.delete(noteUid)
    }

    suspend fun update(note: Note){
        notesDao.update(note)
    }

    suspend fun  insertTodo(todoItem: TodoItem) {
        return notesDao.insertTodo(todoItem)
    }

    suspend fun insertProtected(note : ProtectedNote){
        return notesDao.insertProtected(note)
    }

    fun deleteProtected(note : ProtectedNote){
        return notesDao.deleteProtected(note)
    }

    suspend fun getAllNotes () : List<Note> {
        return notesDao.getAllNotes()
    }




    fun deleteTodo(todoItem: TodoItem){
        notesDao.deleteTodo(todoItem)
    }

    suspend fun updateTodo(todoItem: TodoItem){
        notesDao.updateTodo(todoItem)
    }

    suspend fun insertDeletedNote (note: DeletedNote){
        notesDao.insertDeleted(note)
    }
    fun restoreDeletedNote (note: DeletedNote){
        notesDao.restoreDeleted(note)
    }

    fun getNote(noteUid: String) : LiveData<Note>{
        return notesDao.getNote(noteUid)
    }
    suspend fun getTodoList(noteUid: String) : List<TodoItem>{
        return notesDao.getTodoList(noteUid)
    }

    fun getArchivedNote(noteUid: String) : LiveData<ArchivedNote>{
        return notesDao.getArchivedNote(noteUid)
    }

    fun getProtectedNote(noteUid: String) : LiveData<ProtectedNote>{
        return notesDao.getProtectedNote(noteUid)
    }
    fun getDeletedNote(noteUid: String) : LiveData<DeletedNote>{
        return notesDao.getDeletedNote(noteUid)
    }

    fun getPinnedNote(noteUid: String) : LiveData<PinnedNote>{
        return notesDao.getPinnedNote(noteUid)
    }

    suspend fun insertArchive(archivedNote: ArchivedNote){
        notesDao.insertArchive(archivedNote)
    }
    fun deleteArchive(archivedNote: ArchivedNote){
        notesDao.deleteArchive(archivedNote)
    }

    suspend fun insertPinned(pinnedNote: PinnedNote){
        notesDao.insertPinned(pinnedNote)
    }
    fun deletePinned(pinnedNote: PinnedNote){
        notesDao.deletePinned(pinnedNote)
    }

}