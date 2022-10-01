package com.neuralbit.letsnote.firebaseEntities

data class NoteFire (
    val title: String ="",
    val description: String = "",
    val timeStamp: Long = 0,
    var reminderDate :Long = 0,
    var pinned: Boolean = false,
    var archived: Boolean = false,
    var protected: Boolean = false,
    var deletedDate: Long = 0,
    var tags : List<String> = ArrayList(),
    var todoItems : List<TodoItem> = ArrayList(),
    var selected :Boolean = false,
    var label : Int = 0,
    var noteUid: String ?= "",
    var itemPosition : Int = 0
)