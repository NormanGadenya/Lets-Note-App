package com.neuralbit.letsnote.relationships

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.NoteTagCrossRef
import com.neuralbit.letsnote.entities.Tag

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
