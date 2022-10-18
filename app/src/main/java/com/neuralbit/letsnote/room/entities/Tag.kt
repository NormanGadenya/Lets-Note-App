package com.neuralbit.letsnote.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Tag (
    @PrimaryKey(autoGenerate =false)
    val tagTitle: String
    )