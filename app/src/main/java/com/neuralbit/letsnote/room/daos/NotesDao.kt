package com.neuralbit.letsnote.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.room.entities.*

@Dao
interface NotesDao {
    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insert(note: Note)


    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertProtected(note: ProtectedNote)


    @Update
    suspend fun updateProtected(note: ProtectedNote)

    @Delete
    suspend fun deleteProtected(note: ProtectedNote)


    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertArchive(note : ArchivedNote)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertTodo(todoItems : TodoItem)

    @Update
    suspend fun updateTodo(todoItems : TodoItem)

    @Delete
    suspend fun deleteTodo(todoItems : TodoItem)

    @Delete
    suspend fun deleteArchive(note : ArchivedNote)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertPinned(note : PinnedNote)

    @Delete
    suspend fun deletePinned(note : PinnedNote)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertDeleted(note : DeletedNote)

    @Delete
    suspend fun restoreDeleted(note : DeletedNote)

    @Transaction
    @Query("select * from DeletedNote join Note on DeletedNote.noteUid = Note.noteUid")
    fun getDeletedNotes(): LiveData<List<Note>>

    @Transaction
    @Query("select * from Note where noteUid = :noteUid")
    fun getNote(noteUid : String) : LiveData<Note>

    @Transaction
    @Query("Select * from Note where not exists (select * from ArchivedNote where ArchivedNote.noteUid = Note.noteUid) and not exists (select * from PinnedNote where PinnedNote.noteUid= Note.noteUid) and not exists (select * from DeletedNote where DeletedNote.noteUid = Note.noteUid) order by timeStamp DESC")
    fun getNotesWithoutPinArc(): LiveData<List<Note>>

    @Transaction
    @Query("Select * from Note order by timeStamp DESC ")
    fun getAllNotes(): LiveData<List<Note>>

    @Transaction
    @Query("select * from TodoItem where noteUid = :noteUid ")
    fun getTodoList(noteUid :String): LiveData<List<TodoItem>>

    @Transaction
    @Query("Select * from ArchivedNote join Note on ArchivedNote.noteUid = Note.noteUid and  not exists (select * from DeletedNote where DeletedNote.noteUid = ArchivedNote.noteUid)")
    fun getArchivedNotes() : LiveData<List<Note>>

    @Transaction
    @Query("Select * from PinnedNote  join Note on PinnedNote.noteUid = Note.noteUid where not exists (select * from ArchivedNote where ArchivedNote.noteUid = PinnedNote.noteUid ) and not exists (select * from DeletedNote where DeletedNote.noteUid = PinnedNote.noteUid)")
    fun getPinnedNotes() : LiveData<List<Note>>

    @Transaction
    @Query("Select * from PinnedNote where noteUid = :noteUid")
    fun getPinnedNote(noteUid : String) : LiveData<PinnedNote>

    @Transaction
    @Query("Select * from ProtectedNote where noteUid = :noteUid")
    fun getProtectedNote(noteUid : String) : LiveData<ProtectedNote>

    @Transaction
    @Query("Select * from ArchivedNote where noteUid = :noteUid")
    fun getArchivedNote(noteUid : String) : LiveData<ArchivedNote>

    @Transaction
    @Query("Select * from DeletedNote where noteUid = :noteUid")
    fun getDeletedNote(noteUid : String) : LiveData<DeletedNote>

}