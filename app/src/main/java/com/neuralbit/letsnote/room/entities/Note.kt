package com.neuralbit.letsnote.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @ColumnInfo("title") val title :String,
    @ColumnInfo("description") val description :String,
    @ColumnInfo("timestamp") val timestamp :Long = 0,
    @ColumnInfo("labelColor") val labelColor : Int,
    @PrimaryKey(autoGenerate = false)
    val noteUid :String
)