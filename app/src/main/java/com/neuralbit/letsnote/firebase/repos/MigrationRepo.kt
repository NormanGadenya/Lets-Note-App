package com.neuralbit.letsnote.firebase.repos

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.neuralbit.letsnote.firebase.entities.LabelFire
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.firebase.entities.NoteFireIns
import com.neuralbit.letsnote.firebase.entities.TagFire

class MigrationRepo {

    private val database = Firebase.database
    private val TAG = "NoteFireRepo"


    fun migrateDataAnonymous(oldUser:String, newUser:String){
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

    fun migrateDataRoom(fUserUid : String, notes : ArrayList<NoteFire>, tags : ArrayList<TagFire>, labels : ArrayList<LabelFire>){
        val noteRef = database.getReference(fUserUid).child("notes")
        for (note in notes) {
            val noteFireIns = NoteFireIns(
                title = note.title,
                description = note.description,
                timeStamp = note.timeStamp,
                reminderDate = note.reminderDate,
                pinned = note.pinned,
                archived = note.archived,
                protected = note.protected,
                deletedDate = note.deletedDate,
                tags = note.tags,
                todoItems = note.todoItems,
                label = note.label)
            note.noteUid?.let { noteRef.child(it).setValue(noteFireIns) }
        }
        val tagRef = database.getReference(fUserUid).child("tags")
        for ( tag in tags){
            val tagMap = HashMap<String, Any>()
            tagMap["noteUids"] = tag.noteUids
            tagRef.child(tag.tagName).setValue(tagMap)
        }

        val labelRef = database.getReference(fUserUid).child("labels")
        for ( label in labels){
            val labelMap = HashMap<String, Any>()
            labelMap["noteUids"] = label.noteUids
            labelMap["labelTitle"] = label.labelTitle
            labelRef.child(label.labelColor.toString()).setValue(labelMap)
        }
    }

}