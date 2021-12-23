package com.neuralbit.letsnote.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.neuralbit.letsnote.entities.ArchivedNote
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.PinnedNote
import com.neuralbit.letsnote.entities.Tag

@Dao
interface NotesDao {
    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insertArchive(noteId : ArchivedNote)

    @Delete
    suspend fun deleteArchive(noteId : ArchivedNote)

    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insertPinned(noteId : PinnedNote)

    @Delete
    suspend fun deletePinned(noteId : PinnedNote)

    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insertTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Transaction
    @Query("select * from Tag")
    fun getTags(): LiveData<List<Tag>>

    @Transaction
    @Query("Select * from Note where not exists (select * from ArchivedNote where ArchivedNote.noteID = Note.noteID) and not exists (select * from PinnedNote where PinnedNote.noteID= Note.noteID) order by timeStamp DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Transaction
    @Query("Select * from ArchivedNote join Note on ArchivedNote.noteID = Note.noteID ")
    fun getArchivedNotes() : LiveData<List<Note>>

    @Transaction
    @Query("Select * from PinnedNote  join Note on PinnedNote.noteID = Note.noteID where not exists (select * from ArchivedNote where ArchivedNote.noteID = PinnedNote.noteID ) ")
    fun getPinnedNotes() : LiveData<List<Note>>


}