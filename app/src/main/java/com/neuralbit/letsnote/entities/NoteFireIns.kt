package com.neuralbit.letsnote.entities

data class NoteFireIns(
    val title: String ? ="",
    val description: String ? = "",
    val timeStamp: Long = 0,
    var reminderDate :Long = 0,
    var pinned: Boolean = false,
    var archived: Boolean = false,
    var protected: Boolean = false,
    var deletedDate: Long = 0,
    var tags : List<String> = ArrayList(),
    var todoItems : List<TodoItem> = ArrayList(),
    var label : Int = 0
)