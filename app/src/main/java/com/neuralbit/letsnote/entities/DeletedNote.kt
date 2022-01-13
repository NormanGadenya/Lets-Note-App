package com.neuralbit.letsnote.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeletedNote (
    @PrimaryKey(autoGenerate = false)
    val noteID : Long)