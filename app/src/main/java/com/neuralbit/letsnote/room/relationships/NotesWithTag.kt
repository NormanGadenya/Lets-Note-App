package com.neuralbit.letsnote.room.relationships

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.Tag

data class NotesWithTag(
    @Embedded val tag : Tag,
    @Relation(
        parentColumn = "tagTitle",
        entityColumn = "noteUid",
        associateBy = Junction(NoteTagCrossRef::class)
    )
    val notes: List<Note>
    //Gives list of notes for a particular Tag ID
)
