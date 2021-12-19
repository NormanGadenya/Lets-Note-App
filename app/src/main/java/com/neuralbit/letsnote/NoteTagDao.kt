package com.neuralbit.letsnote

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteTagDao {
    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Transaction
    @Query("select * from TagTable where tagID = :tagID")
    suspend fun getNotesWithTag(tagID:Int) : LiveData<List<NotesWithTag>>

    @Transaction
    @Query("select * from NotesTable where noteID = :noteID")
    suspend fun getTagsWithNote(noteID:Int) : LiveData<List<TagsWithNote>>


}