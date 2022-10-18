package com.neuralbit.letsnote.room.relationships

import androidx.room.Embedded
import androidx.room.Relation
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.TodoItem

data class NoteWithTodo(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "noteUid",
        entityColumn = "noteUid"
    )
    val todoItems : List<TodoItem>
)
