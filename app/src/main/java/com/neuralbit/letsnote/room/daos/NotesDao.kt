package com.neuralbit.letsnote.room.daos

import androidx.room.*
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.TodoItem

@Dao
interface NotesDao {
    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insert(note: Note)


    @Update
    suspend fun update(note: Note)

    @Query("delete from Note where noteUid = :noteUid")
    fun delete(noteUid: String)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertTodo(todoItems : TodoItem)

    @Update
    suspend fun updateTodo(todoItems : TodoItem)

    @Delete
    fun deleteTodo(todoItems : TodoItem)


    @Transaction
    @Query("select * from Note where noteUid = :noteUid")
    suspend fun getNote(noteUid : String) : Note

    @Transaction
    @Query("Select * from Note order by timeStamp DESC ")
    fun getAllNotes(): List<Note>

    @Transaction
    @Query("select * from TodoItem where noteUid = :noteUid ")
    suspend fun getTodoList(noteUid :String): List<TodoItem>



}