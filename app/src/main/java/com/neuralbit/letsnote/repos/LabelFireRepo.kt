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
import com.neuralbit.letsnote.entities.LabelFire

class LabelFireRepo {
    val database = Firebase.database
    val TAG = "LabelFireRepo"

    private val fUser = FirebaseAuth.getInstance().currentUser

    fun getAllLabels () : LiveData<List<LabelFire>> {
        val live = MutableLiveData<List<LabelFire>>()
        val labelRef = fUser?.let { database.getReference(it.uid).child("labels") }
        labelRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val labels = ArrayList<LabelFire>()
                for ( s : DataSnapshot in snapshot.children ){
                    val label = s.getValue(LabelFire::class.java)
                    if (label != null) {
                        label.labelColor = s.key!!.toInt()
                        labels.add(label)
                    }
                }
                live.value = labels
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}" )
            }
        })
        fUser?.let { database.getReference(it.uid).keepSynced(true) }
        return live
    }


    fun addOrDeleteLabels(label : Int, noteUid: String, add : Boolean) {
        val labelRef = fUser?.let { database.getReference(it.uid).child("labels")}
        labelRef?.child(label.toString())?.addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val labelFire = snapshot.getValue(LabelFire::class.java)
                    if (labelFire != null) {
                        val noteUids = labelFire.noteUids
                        if (add) {
                            noteUids.add(noteUid)
                            val updateMap = HashMap<String, Any>()
                            updateMap["noteUids"] = noteUids
                            labelRef.child(label.toString()).updateChildren(updateMap)
                        }else {
                            noteUids.remove(noteUid)
                            val updateMap = HashMap<String, Any>()
                            updateMap["noteUids"] = noteUids
                            if (noteUids.isNotEmpty()){
                                labelRef.child(label.toString()).updateChildren(updateMap)
                            }else{
                                labelRef.child(label.toString()).removeValue()
                            }
                        }
                    }

                } else {
                    val newTagMap = HashMap<String, Any>()
                    val noteUids = ArrayList<String>()
                    noteUids.add(noteUid)
                    newTagMap["noteUids"] = noteUids
                    labelRef.child(label.toString()).setValue(newTagMap)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: $error",)
            }
        })
    }

}
