package com.neuralbit.letsnote.repos

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MigrationRepo {

    private val database = Firebase.database
    private val TAG = "NoteFireRepo"


    fun migrateData( oldUser:String, newUser:String){
        val oldRef = database.getReference(oldUser)
        val newRef = database.getReference(newUser)

        oldRef.addListenerForSingleValueEvent( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (oldUser != newUser){
                    newRef.setValue(snapshot.value).addOnSuccessListener {
                        oldRef.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: $error")
            }
        })

    }

}