package com.neuralbit.letsnote.daos

import androidx.room.*
import com.neuralbit.letsnote.relationships.NotesWithTag
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.entities.NoteTagCrossRef

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
    suspend fun getTagsWithNote(noteID: Long) : List<TagsWithNote>


}