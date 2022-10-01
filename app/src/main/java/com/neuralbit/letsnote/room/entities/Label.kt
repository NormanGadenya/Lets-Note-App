package com.neuralbit.letsnote.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Label(
    @PrimaryKey(autoGenerate =false)
    val labelColor: Int,

    val labelTitle: String,



)