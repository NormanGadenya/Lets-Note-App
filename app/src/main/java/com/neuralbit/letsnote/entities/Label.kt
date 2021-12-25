package com.neuralbit.letsnote.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Label(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo val noteID : Long,
    @ColumnInfo val labelID : Int
)
