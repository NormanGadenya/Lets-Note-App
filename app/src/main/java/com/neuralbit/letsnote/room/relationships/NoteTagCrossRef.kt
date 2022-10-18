package com.neuralbit.letsnote.room.relationships

import androidx.room.Entity

@Entity(primaryKeys = ["noteUid","tagTitle"])
data class NoteTagCrossRef(
    val noteUid: String,
    val tagTitle: String
)
