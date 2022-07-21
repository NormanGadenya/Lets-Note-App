package com.neuralbit.letsnote.repos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.NoteFire

class NoteFireRepo {

    val database = Firebase.database
    val TAG = "NoteFireRepo"

    private val fUser = FirebaseAuth.getInstance().currentUser

    fun addNote( note : Note){
        val notesRef = fUser?.let { database.getReference(it.uid) }
        notesRef?.child("notes")?.push()?.setValue(note)
    }

    fun getAllNotes () : LiveData<List<Note>> {
        val live = MutableLiveData<List<Note>>()
        val notesRef = fUser?.let { database.getReference(it.uid).child("notes") }
        notesRef?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = ArrayList<Note>()
                for ( s : DataSnapshot in snapshot.children ){
                    val note = s.getValue(NoteFire::class.java)
                    if (note != null) {
                        val n = Note(note.title,note.description,note.timeStamp)
                        notes.add(n)
                    }
                }
                live.value = notes
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}" )
            }
        })
        return live
    }

}