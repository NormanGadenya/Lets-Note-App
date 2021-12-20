package com.neuralbit.letsnote

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteTagDao {
    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    suspend fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Transaction
    @Query("select * from TagTable where tagTitle = :tagTitle")
    suspend fun getNotesWithTag(tagTitle:String) : List<NotesWithTag>

    @Transaction
    @Query("select * from NotesTable where noteID = :noteID")
    suspend fun getTagsWithNote(noteID:Int) : List<TagsWithNote>


}