package com.neuralbit.letsnote.room.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.neuralbit.letsnote.room.entities.Label
import com.neuralbit.letsnote.room.entities.Note

data class LabelWIthNotes(
    @Embedded val label : Label,
    @Relation(
        parentColumn = "labelColor",
        entityColumn = "labelColor"
    )
    val notes : List<Note>
)
