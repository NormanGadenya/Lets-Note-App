package com.neuralbit.letsnote.repos

data class NoteFire (
    val title: String ="",
    val description: String = "",
    val timeStamp: Long = 0,
    var reminderDate :Long = 0,
    var pinned: Boolean = false,
    var tags : List<String> = ArrayList(),
    var label : Int = 0,
    var noteUid: String ?= ""
)