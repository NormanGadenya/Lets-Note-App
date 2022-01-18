package com.neuralbit.letsnote.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoItem(
    val noteID : Long,
    val itemDesc : String,
    val itemChecked : Boolean

){
    @PrimaryKey(autoGenerate = true)
    var itemID : Long =0
}
