package com.neuralbit.letsnote

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotesDao {
    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insert(note:Note)

    @Update
    suspend fun update(note:Note)

    @Delete
    suspend fun delete(note:Note)

    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insertArchive(noteId : ArchivedNote)

    @Delete
    suspend fun deleteArchive(noteId : ArchivedNote)

    @Query("Select * from NotesTable where not exists (select * from ArchivedNotesTable where ArchivedNotesTable.id = NotesTable.id) order by timeStamp DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("Select * from ArchivedNotesTable join NotesTable on ArchivedNotesTable.id = NotesTable.id  ")
    fun getArchivedNotes() : LiveData<List<Note>>

}