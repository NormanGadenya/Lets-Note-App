package com.neuralbit.letsnote

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NotesWithTag(
    @Embedded val tag : Tag,
    @Relation(
        parentColumn = "tagTitle",
        entityColumn = "noteID",
        associateBy = Junction(NoteTagCrossRef::class)
    )
    val notes: List<Note>
    //Gives list of notes for a particular Tag ID
)
