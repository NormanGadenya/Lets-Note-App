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

    @Query("select * from TagTable")
    fun getTags(): LiveData<List<Tag>>


    @Query("Select * from NotesTable where not exists (select * from ArchivedNotesTable where ArchivedNotesTable.id = NotesTable.noteID) and not exists (select * from PinnedNotesTable where PinnedNotesTable.id= NotesTable.noteID) order by timeStamp DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("Select * from ArchivedNotesTable join NotesTable on ArchivedNotesTable.id = NotesTable.noteID ")
    fun getArchivedNotes() : LiveData<List<Note>>

    @Query("Select * from PinnedNotesTable join NotesTable on PinnedNotesTable.id = NotesTable.noteID")
    fun getPinnedNotes() : LiveData<List<Note>>


}