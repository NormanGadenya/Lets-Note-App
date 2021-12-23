package com.neuralbit.letsnote.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tag (
    @PrimaryKey(autoGenerate = false)
    val tagTitle: String
    )