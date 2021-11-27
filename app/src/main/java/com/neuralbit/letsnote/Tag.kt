package com.neuralbit.letsnote

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TagTable")
data class Tag (
    @ColumnInfo(name = "tagTitle")val tagTitle: String,

    ){

    @PrimaryKey(autoGenerate = true)
    var id=0
}