package com.neuralbit.letsnote.firebase.entities

data class LabelFire(
    var labelColor : Int = 0,
    var labelTitle: String ="",
    var noteUids :ArrayList <String> = ArrayList()
)