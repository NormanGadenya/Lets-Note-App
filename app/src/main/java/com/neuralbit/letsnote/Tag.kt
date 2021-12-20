package com.neuralbit.letsnote

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TagTable")
data class Tag (
    @PrimaryKey(autoGenerate = false)
    val tagTitle: String
    )