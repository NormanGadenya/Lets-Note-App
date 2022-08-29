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
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.utilities.NoteComparator

class NoteFireRepo {

    private val database = Firebase.database
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


    fun getAllNotes () : LiveData<ArrayList<NoteFire>> {
        val live = MutableLiveData<ArrayList<NoteFire>>()
        val notesRef = fUser?.let { database.getReference(it.uid).child("notes") }

        notesRef?.addListenerForSingleValueEvent(object : ValueEventListener{
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

    fun migrateData( oldUser:String, newUser:String){

        val notesRef = database.getReference(oldUser).child("notes")
        notesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = ArrayList<NoteFire>()
                for ( s : DataSnapshot in snapshot.children ){

                    val note = s.getValue(NoteFire::class.java)
                    if (note != null) {
                        note.noteUid = s.key
                        notes.add(note)
                    }
                }
                val notesUpdate = HashMap<String,Any>()
                notesUpdate["notes"] = notes
                database.getReference(newUser).updateChildren(notesUpdate)

                database.getReference(oldUser).child("notes").removeValue()


            }

            override fun onCancelled(error: DatabaseError) {
                throw error.toException()
            }
        })

        val labelRef = database.getReference(oldUser).child("labels")

        labelRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val labels = ArrayList<LabelFire>()
                for ( s : DataSnapshot in snapshot.children ){
                    val label = s.getValue(LabelFire::class.java)
                    if (label != null) {
                        label.labelColor = s.key!!.toInt()
                        labels.add(label)
                    }
                }

                for (l in labels) {
                    val labelUpdate = HashMap<String,Any>()
                    val label = LabelIns(labelTitle = l.labelTitle, noteUids = l.noteUids)
                    labelUpdate[l.labelColor.toString()] = label
                    database.getReference(newUser).updateChildren(labelUpdate)
                }

                database.getReference(oldUser).child("labels").removeValue()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}" )
            }
        })

        val tagRef = database.getReference(oldUser).child("tags")


        tagRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val tags = ArrayList<TagFire>()
            for ( s : DataSnapshot in snapshot.children ){
                val tagFire = s.getValue(TagFire::class.java)
                if (tagFire != null) {
                    tagFire.tagName = s.key.toString()
                    tags.add(tagFire)
                }
            }

            for (t in tags) {
                val tagUpdate = HashMap<String,Any>()
                tagUpdate["noteUids"] = t.noteUids
                database.getReference(newUser).child(t.tagName).updateChildren(tagUpdate)
            }

            database.getReference(oldUser).child("tags").removeValue()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled: ${error.message}" )
        }
    })


}

}