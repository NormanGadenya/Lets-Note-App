package com.neuralbit.letsnote.relationships

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.NoteTagCrossRef
import com.neuralbit.letsnote.entities.Tag

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
