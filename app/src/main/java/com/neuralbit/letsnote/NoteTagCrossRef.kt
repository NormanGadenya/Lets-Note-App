package com.neuralbit.letsnote

import androidx.room.Entity

@Entity(primaryKeys = ["noteID","tagID"])
data class NoteTagCrossRef(
    val noteID : Int,
    val tagID : Int
)
