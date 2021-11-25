package com.neuralbit.letsnote.ui

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "noteTagTable")
class NoteTag (
    @PrimaryKey val noteID : Int,
    @ColumnInfo(name = "tagID")var tagID: Int
        ){

}