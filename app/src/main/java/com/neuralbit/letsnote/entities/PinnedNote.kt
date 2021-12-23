package com.neuralbit.letsnote.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "PinnedNotesTable")
class PinnedNote(
    @PrimaryKey @ColumnInfo(name = "noteID") val noteID: Long
)