package com.neuralbit.letsnote.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ArchivedNotesTable")
class ArchivedNote(
    @PrimaryKey @ColumnInfo(name = "noteID") val noteID: Long
)
