package com.neuralbit.letsnote.repos

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MigrationRepo {

    private val database = Firebase.database
    val TAG = "NoteFireRepo"


    fun migrateData( oldUser:String, newUser:String){

//        database.getReference(oldUser).addListenerForSingleValueEvent(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.d(TAG, "onDataChange: $snapshot")
//                val notesUpdate = HashMap<String,Any>()
//                notesUpdate[newUser] = snapshot.children
//                database.getReference(newUser).updateChildren(notesUpdate)
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
//            }
//        })
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
//        val notesRef = database.getReference(oldUser).child("notes")
//        notesRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val notes = ArrayList<NoteFire>()
//                Log.d(TAG, "onDataChange: $snapshot")
//
//                for ( s : DataSnapshot in snapshot.children ){
//                    Log.d(TAG, "onDataChange: $s")
//
//                    val note = s.getValue(NoteFire::class.java)
//                    if (note != null) {
//                        note.noteUid = s.key
//                        notes.add(note)
//                    }
//                }
//                val notesUpdate = HashMap<String,Any>()
//                notesUpdate["notes"] = notes
//                database.getReference(newUser).updateChildren(notesUpdate)
//
//                database.getReference(oldUser).child("notes").removeValue()
//
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                throw error.toException()
//            }
//
//        })
//
//        val labelRef = database.getReference(oldUser).child("labels")
//
//        labelRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val labels = ArrayList<LabelFire>()
//                for ( s : DataSnapshot in snapshot.children ){
//                    val label = s.getValue(LabelFire::class.java)
//                    if (label != null) {
//                        label.labelColor = s.key!!.toInt()
//                        labels.add(label)
//                    }
//                }
//
//                for (l in labels) {
//                    val labelUpdate = HashMap<String,Any>()
//                    val label = LabelIns(labelTitle = l.labelTitle, noteUids = l.noteUids)
//                    labelUpdate[l.labelColor.toString()] = label
//                    database.getReference(newUser).updateChildren(labelUpdate)
//                }
//
//                database.getReference(oldUser).child("labels").removeValue()
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e(TAG, "onCancelled: ${error.message}" )
//            }
//        })
//
//        val tagRef = database.getReference(oldUser).child("tags")
//
//
//        tagRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val tags = ArrayList<TagFire>()
//                for ( s : DataSnapshot in snapshot.children ){
//                    val tagFire = s.getValue(TagFire::class.java)
//                    if (tagFire != null) {
//                        tagFire.tagName = s.key.toString()
//                        tags.add(tagFire)
//                    }
//                }
//
//                for (t in tags) {
//                    val tagUpdate = HashMap<String,Any>()
//                    tagUpdate["noteUids"] = t.noteUids
//                    database.getReference(newUser).child(t.tagName).updateChildren(tagUpdate)
//                }
//
//                database.getReference(oldUser).child("tags").removeValue()
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e(TAG, "onCancelled: ${error.message}" )
//            }
//        })


    }

}