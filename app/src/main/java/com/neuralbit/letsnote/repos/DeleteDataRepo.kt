package com.neuralbit.letsnote.repos

import android.content.Context
import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DeleteDataRepo(val context : Context) {

    private val database = Firebase.database
    val TAG = "NoteFireRepo"
    private val fUser = FirebaseAuth.getInstance().currentUser



    fun deleteUserData( ){
        val userRef = fUser?.uid?.let { database.getReference(it) }
        userRef?.removeValue()
        AuthUI.getInstance()
            .signOut(context)
            .addOnCompleteListener{
                val a = Intent(Intent.ACTION_MAIN)
                a.addCategory(Intent.CATEGORY_HOME)
                a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(a)
            }

    }


}