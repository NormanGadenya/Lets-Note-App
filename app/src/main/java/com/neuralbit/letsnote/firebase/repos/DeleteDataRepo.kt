package com.neuralbit.letsnote.firebase.repos

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DeleteDataRepo() {

    private val database = Firebase.database
    val TAG = "NoteFireRepo"
    private val fUser = FirebaseAuth.getInstance().currentUser



    fun deleteUserData( ){
        val userRef = fUser?.uid?.let { database.getReference(it) }
        userRef?.removeValue()
    }


}