package com.neuralbit.letsnote.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Reminder (
    @PrimaryKey(autoGenerate = false)
    var noteUid: String,
    var time : Long
        )