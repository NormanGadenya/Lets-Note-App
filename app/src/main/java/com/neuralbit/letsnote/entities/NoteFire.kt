package com.neuralbit.letsnote.entities

data class NoteFire (
    val title: String ="",
    val description: String = "",
    val timeStamp: Long = 0,
    var reminderDate :Long = 0,
    var pinned: Boolean = false,
    var deleted: Boolean = false,
    var archived: Boolean = false,
    var protected: Boolean = false,
    var tags : List<String> = ArrayList(),
    var label : Int = 0,
    var noteUid: String ?= ""
)