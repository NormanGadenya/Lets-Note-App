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
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.entities.NoteFireIns
import com.neuralbit.letsnote.utilities.NoteComparator

class NoteFireRepo {

    val database = Firebase.database
    val TAG = "NoteFireRepo"

    private val fUser = FirebaseAuth.getInstance().currentUser

    fun addNote( note : NoteFireIns) : String ?{
        val notesRef = fUser?.let { database.getReference(it.uid).child("notes")}
        val key = notesRef?.push()?.key
        if (key != null) {
            notesRef.child(key).setValue(note)
        }
        return key
    }

    fun getNote( noteUid : String) : LiveData<NoteFire?> {
        val live = MutableLiveData<NoteFire?>()
        val notesRef = fUser?.let { database.getReference(it.uid).child("notes").child(noteUid) }
        notesRef?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val note = snapshot.getValue(NoteFire::class.java)
                live.value = note
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}" )
            }
        })
        fUser?.let { database.getReference(it.uid).keepSynced(true) }
        return live
    }

    fun getAllNotes () : LiveData<ArrayList<NoteFire>> {
        val live = MutableLiveData<ArrayList<NoteFire>>()
        val notesRef = fUser?.let { database.getReference(it.uid).child("notes") }
        notesRef?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = ArrayList<NoteFire>()
                for ( s : DataSnapshot in snapshot.children ){
                    Log.d(TAG, "onDataChange: $s")
                    val note = s.getValue(NoteFire::class.java)
                    if (note != null) {
                        note.noteUid = s.key
                        notes.add(note)
                    }
                }

                notes.sortWith(NoteComparator())
                live.value = notes
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}" )
            }
        })
        return live
    }

    fun updateNote(noteUpdate : Map<String,Any>, noteUid : String) {
        fUser?.let {
            val noteRef = database.reference.child(it.uid).child("notes").child(noteUid)
            noteRef.updateChildren(noteUpdate)
        }
    }

    fun deleteNote ( noteUid : String){
        val notesRef = fUser?.let { database.getReference(it.uid).child("notes") }
        notesRef?.child(noteUid)?.removeValue()

    }

}