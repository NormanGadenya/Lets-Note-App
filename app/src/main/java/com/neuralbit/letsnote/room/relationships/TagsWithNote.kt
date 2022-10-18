package com.neuralbit.letsnote.room.relationships

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.Tag

data class TagsWithNote(
    @Embedded val note : Note,
    @Relation(
        parentColumn = "noteUid",
        entityColumn = "tagTitle",
        associateBy = Junction(NoteTagCrossRef::class)
    )
    val tags: List<Tag>
    //Gives list of tags for a particular note ID
)
