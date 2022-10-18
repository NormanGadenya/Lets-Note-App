package com.neuralbit.letsnote.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TodoItem (
    var noteUid : String,
    var itemDesc : String,
    var itemChecked : Boolean,

    @PrimaryKey(autoGenerate = true)
    var itemId : Long = 0
        )