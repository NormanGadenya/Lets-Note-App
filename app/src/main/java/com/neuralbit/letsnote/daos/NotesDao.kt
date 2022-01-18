package com.neuralbit.letsnote.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.entities.*

@Dao
interface NotesDao {
    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insert(note: Note) : Long


    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertArchive(noteId : ArchivedNote)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertTodo(todoItems : TodoItem)

    @Update
    suspend fun updateTodo(todoItems : TodoItem)

    @Delete
    suspend fun deleteTodo(todoItems : TodoItem)

    @Delete
    suspend fun deleteArchive(noteId : ArchivedNote)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertPinned(noteId : PinnedNote)

    @Delete
    suspend fun deletePinned(noteId : PinnedNote)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertDeleted(noteId : DeletedNote)

    @Delete
    suspend fun restoreDeleted(noteId : DeletedNote)

    @Transaction
    @Query("select * from DeletedNote join Note on DeletedNote.noteID = Note.noteID")
    fun getDeletedNotes(): LiveData<List<Note>>

    @Transaction
    @Query("select * from Note where noteID = :noteID")
    fun getNote(noteID : Long) : LiveData<Note>

    @Transaction
    @Query("Select * from Note where not exists (select * from ArchivedNote where ArchivedNote.noteID = Note.noteID) and not exists (select * from PinnedNote where PinnedNote.noteID= Note.noteID) and not exists (select * from DeletedNote where DeletedNote.noteID = Note.noteID) order by timeStamp DESC")
    fun getNotesWithoutPinArc(): LiveData<List<Note>>

    @Transaction
    @Query("Select * from Note where not exists (select * from DeletedNote where DeletedNote.noteID = Note.noteID) order by timeStamp DESC ")
    fun getAllNotes(): LiveData<List<Note>>



    @Transaction
    @Query("Select * from ArchivedNote join Note on ArchivedNote.noteID = Note.noteID and  not exists (select * from DeletedNote where DeletedNote.noteID = ArchivedNote.noteID)")
    fun getArchivedNotes() : LiveData<List<Note>>

    @Transaction
    @Query("Select * from PinnedNote  join Note on PinnedNote.noteID = Note.noteID where not exists (select * from ArchivedNote where ArchivedNote.noteID = PinnedNote.noteID ) and not exists (select * from DeletedNote where DeletedNote.noteID = PinnedNote.noteID)")
    fun getPinnedNotes() : LiveData<List<Note>>

    @Transaction
    @Query("Select * from PinnedNote where noteID = :noteID")
    fun getPinnedNote(noteID : Long) : LiveData<PinnedNote>

    @Transaction
    @Query("Select * from ArchivedNote where noteID = :noteID")
    fun getArchivedNote(noteID : Long) : LiveData<ArchivedNote>

    @Transaction
    @Query("Select * from DeletedNote where noteID = :noteID")
    fun getDeletedNote(noteID : Long) : LiveData<DeletedNote>


}