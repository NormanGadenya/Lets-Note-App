package com.neuralbit.letsnote

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ArchivedNotesTable")
class ArchivedNote(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long
)
