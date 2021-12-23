package com.neuralbit.letsnote.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "PinnedNotesTable")
class PinnedNote(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long
)