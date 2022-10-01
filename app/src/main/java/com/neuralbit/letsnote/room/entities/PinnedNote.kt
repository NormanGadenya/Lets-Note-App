package com.neuralbit.letsnote.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class PinnedNote(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "noteUid") var noteUid: String
)