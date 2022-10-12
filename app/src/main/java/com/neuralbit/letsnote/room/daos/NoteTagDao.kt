package com.neuralbit.letsnote.room.daos

import androidx.room.*
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.relationships.NotesWithTag
import com.neuralbit.letsnote.room.relationships.TagsWithNote

@Dao
interface NoteTagDao {
    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Delete
    fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef)

    @Transaction
    @Query("select * from Tag where tagTitle = :tagTitle")
    suspend fun getNotesWithTag(tagTitle:String) : List<NotesWithTag>

    @Transaction
    @Query("select * from Note where noteUid = :noteUid")
    suspend fun getTagsWithNote(noteUid: String) : List<TagsWithNote>




}