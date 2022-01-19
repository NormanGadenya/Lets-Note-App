package com.neuralbit.letsnote.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoItem(
    var noteID : Long,
    var itemDesc : String,
    var itemChecked : Boolean

){
    @PrimaryKey(autoGenerate = true)
    var itemID : Long =0
}
