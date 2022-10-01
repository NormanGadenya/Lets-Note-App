package com.neuralbit.letsnote.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.room.entities.ArchivedNote
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.PinnedNote

@Dao
interface NotesDao {
    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertArchive(note : ArchivedNote)

    @Delete
    suspend fun deleteArchive(note : ArchivedNote)

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertPinned(note : PinnedNote)

    @Delete
    suspend fun deletePinned(note : PinnedNote)

    @Transaction
    @Query("select * from Note where noteUid = :noteUid")
    fun getNote(noteUiD : String) : LiveData<Note>

    @Transaction
    @Query("Select * from Note where not exists (select * from ArchivedNote where ArchivedNote.noteUid = Note.noteUid) and not exists (select * from PinnedNote where PinnedNote.noteUid= Note.noteUid) order by timeStamp DESC")
    fun getNotesWithoutPinArc(): LiveData<List<Note>>

    @Transaction
    @Query("Select * from Note  order by timeStamp DESC")
    fun getAllNotes(): LiveData<List<Note>>


    @Transaction
    @Query("Select * from ArchivedNote join Note on ArchivedNote.noteUid = Note.noteUid ")
    fun getArchivedNotes() : LiveData<List<Note>>

    @Transaction
    @Query("Select * from PinnedNote join Note on PinnedNote.noteUid = Note.noteUid where not exists (select * from ArchivedNote where ArchivedNote.noteUid = PinnedNote.noteUid ) ")
    fun getPinnedNotes() : LiveData<List<Note>>

    @Transaction
    @Query("Select * from PinnedNote where noteUid = :noteUid")
    fun getPinnedNote(noteUid : String) : LiveData<PinnedNote>

    @Transaction
    @Query("Select * from ArchivedNote where noteUid = :noteUid")
    fun getArchivedNote(noteUid : String) : LiveData<ArchivedNote>

}