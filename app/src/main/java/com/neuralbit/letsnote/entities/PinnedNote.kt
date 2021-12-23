package com.neuralbit.letsnote.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class PinnedNote(
    @PrimaryKey (autoGenerate = false)
    @ColumnInfo(name = "noteID") val noteID: Long
)