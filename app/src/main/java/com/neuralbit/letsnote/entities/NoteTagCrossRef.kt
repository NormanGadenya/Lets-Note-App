package com.neuralbit.letsnote.entities

import androidx.room.Entity

@Entity(primaryKeys = ["noteID","tagTitle"])
data class NoteTagCrossRef(
    val noteID: Long,
    val tagTitle: String
)
