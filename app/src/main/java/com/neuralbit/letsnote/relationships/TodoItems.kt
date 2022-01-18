package com.neuralbit.letsnote.relationships
import androidx.room.Embedded
import androidx.room.Relation
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.TodoItem


data class TodoItems(
    @Embedded val note : Note,
    @Relation(
        parentColumn = "noteID",
        entityColumn = "noteID"
    )
    val todoItems : List<TodoItem>
)