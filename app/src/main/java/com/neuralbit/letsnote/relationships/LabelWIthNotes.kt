package com.neuralbit.letsnote.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.entities.Note

data class LabelWIthNotes(
    @Embedded val label : Label,
    @Relation(
        parentColumn = "noteID",
        entityColumn = "noteID"
    )
    val notes : List<Note>
)
