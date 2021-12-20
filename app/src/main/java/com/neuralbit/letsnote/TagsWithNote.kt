package com.neuralbit.letsnote

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TagsWithNote(
    @Embedded val note : Note,
    @Relation(
        parentColumn = "noteID",
        entityColumn = "tagTitle",
        associateBy = Junction(NoteTagCrossRef::class)
    )
    val tags: List<Tag>
    //Gives list of tags for a particular note ID
)
