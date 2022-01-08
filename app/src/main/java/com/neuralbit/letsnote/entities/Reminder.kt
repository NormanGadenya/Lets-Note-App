package com.neuralbit.letsnote.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.sql.Time

@Entity
data class Reminder(
    @PrimaryKey ( autoGenerate = false)
    @ColumnInfo var noteID: Long,
    @ColumnInfo val dateTime : Long

    )
