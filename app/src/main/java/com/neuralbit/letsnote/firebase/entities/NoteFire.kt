package com.neuralbit.letsnote.firebase.entities

data class NoteFire (
    var title: String ="",
    var description: String = "",
    var timeStamp: Long = 0,
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