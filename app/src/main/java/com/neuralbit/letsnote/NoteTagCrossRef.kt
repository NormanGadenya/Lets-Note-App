package com.neuralbit.letsnote

import androidx.room.Entity

@Entity(primaryKeys = ["noteID","tagTitle"])
data class NoteTagCrossRef(
    val noteID: Long,
    val tagTitle: String
)
