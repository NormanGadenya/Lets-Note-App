package com.neuralbit.letsnote

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TagTable")
class Tag (
    @ColumnInfo(name = "tagTitle")val tagTitle: String,
    @ColumnInfo(name = "tagColor")val tagColor: Int?
    ){

    @PrimaryKey(autoGenerate = true)
    val tagID = 0
}