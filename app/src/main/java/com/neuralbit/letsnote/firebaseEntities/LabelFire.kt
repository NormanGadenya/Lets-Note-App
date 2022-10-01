package com.neuralbit.letsnote.firebaseEntities

data class LabelFire(
    var labelColor : Int = 0,
    var labelTitle: String="",
    var noteUids :ArrayList <String> = ArrayList()
)