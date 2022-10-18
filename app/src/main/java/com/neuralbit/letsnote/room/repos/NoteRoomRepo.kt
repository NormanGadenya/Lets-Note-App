package com.neuralbit.letsnote.room.repos

import com.neuralbit.letsnote.room.daos.NotesDao
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.TodoItem

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

    suspend fun getAllNotes () : List<Note> {
        return notesDao.getAllNotes()
    }

    suspend fun getNote( noteUid: String) : Note {
        return notesDao.getNote(noteUid)
    }

    suspend fun deleteAllNotes(){
        notesDao.deleteAllNotes()
    }

    suspend fun deleteAllTodoItems(){
        notesDao.deleteAllTodos()
    }

    fun deleteTodo(todoItem: TodoItem){
        notesDao.deleteTodo(todoItem)
    }

    suspend fun getTodoList(noteUid: String) : List<TodoItem>{
        return notesDao.getTodoList(noteUid)
    }


}