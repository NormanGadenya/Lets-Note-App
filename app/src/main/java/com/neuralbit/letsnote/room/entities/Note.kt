package com.neuralbit.letsnote.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @ColumnInfo(name = "title") val title :String ?,
    @ColumnInfo(name = "description") val description :String ?,
    @ColumnInfo(name = "timestamp") val timestamp :Long = 0,
    @ColumnInfo(name = "labelColor") val labelColor : Int,
    @PrimaryKey(autoGenerate = false)
    val noteUid :String
)