package com.neuralbit.letsnote.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    val title :String? = "",
    val description :String ? = "",
    val timestamp :Long = 0,
    var labelColor : Int = 0,
    val archived : Boolean = false,
    val pinned : Boolean = false,
    val locked : Boolean = false,
    val deletedDate : Long = 0,
    val reminderDate : Long = 0,
    @PrimaryKey(autoGenerate = false)
    val noteUid :String
)