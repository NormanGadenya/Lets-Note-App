package com.neuralbit.letsnote.firebase.entities

data class TagFire(
    var tagName : String = "",
    var noteUids :ArrayList <String> = ArrayList()
)