package com.neuralbit.letsnote.firebaseRepos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseEntities.NoteFireIns
import com.neuralbit.letsnote.utilities.NoteComparator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteFireRepo {

    private val database = Firebase.database
    val TAG = "NoteFireRepo"

    private var fUser = FirebaseAuth.getInstance().currentUser

    fun addNote( note : NoteFireIns) : String ?{
        val notesRef = fUser?.let { database.getReference(it.uid).child("notes")}
        val key = notesRef?.push()?.key
        if (key != null) {
            notesRef.child(key).setValue(note)
        }
        return key
    }


    suspend fun getAllNotes () : LiveData<ArrayList<NoteFire>> {
        val live = MutableLiveData<ArrayList<NoteFire>>()
        withContext(Dispatchers.Main){
            var notesRef = fUser?.let { database.getReference(it.uid).child("notes") }
            val eventListener = object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notes = ArrayList<NoteFire>()
                    for ( s : DataSnapshot in snapshot.children ){

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
                    throw error.toException()

                }
            }
            notesRef?.addValueEventListener(eventListener)
            FirebaseAuth.getInstance().addAuthStateListener {
                notesRef?.removeEventListener(eventListener)
                fUser= it.currentUser
                notesRef = it.currentUser?.uid?.let { it1 -> database.getReference(it1).child("notes") }

                notesRef?.addValueEventListener(eventListener)

            }
        }


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