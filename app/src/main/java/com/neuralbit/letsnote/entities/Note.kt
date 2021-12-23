package com.neuralbit.letsnote.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Note(
    @ColumnInfo(name = "title")val title: String ?,
    @ColumnInfo(name ="description")val description: String ?,
    @ColumnInfo(name ="timeStamp")val timeStamp: Long,
)
{
    @PrimaryKey(autoGenerate = true)
    var noteID : Long =0
    }

