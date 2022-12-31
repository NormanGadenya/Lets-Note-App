package com.neuralbit.letsnote.firebase.repos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.neuralbit.letsnote.firebase.entities.LabelFire

class LabelFireRepo {
    private val database = Firebase.database
    private val TAG = "LabelFireRepo"

    private var fUser = FirebaseAuth.getInstance().currentUser

    fun getAllLabels () : LiveData<List<LabelFire>> {
        val live = MutableLiveData<List<LabelFire>>()

        var labelRef = fUser?.let { database.getReference(it.uid).child("labels") }

        val eventListener = object :ValueEventListener{
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
                throw error.toException()
            }
        }
        labelRef?.addValueEventListener(eventListener)
        FirebaseAuth.getInstance().addAuthStateListener {
            labelRef?.removeEventListener(eventListener)
            fUser= it.currentUser
            labelRef = it.currentUser?.uid?.let { it1 -> database.getReference(it1).child("labels") }

            labelRef?.addValueEventListener(eventListener)

        }
        return live
    }


    fun addOrDeleteLabels(newLabel : Int, oldLabel : Int, noteUid: String, labelTitle : String?, add : Boolean) {
        val labelRef = fUser?.let { database.getReference(it.uid).child("labels")}

        labelRef?.addListenerForSingleValueEvent( object  : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                var exists = false
                for (snapshot in s.children){
                    val labelFire = snapshot.getValue(LabelFire::class.java)
                    if (snapshot.key == newLabel.toString()){
                        if (labelFire != null) {
                            val noteUids = labelFire.noteUids
                            if (add && newLabel > 0) {
                                if (!noteUids.contains(noteUid)){
                                    noteUids.add(noteUid)
                                    val updateMap = HashMap<String, Any>()
                                    updateMap["noteUids"] = noteUids
                                    labelRef.child(newLabel.toString()).updateChildren(updateMap)
                                }
                            }else {
                                noteUids.remove(noteUid)
                                val updateMap = HashMap<String, Any>()
                                updateMap["noteUids"] = noteUids
                                if (noteUids.isNotEmpty()){
                                    labelRef.child(newLabel.toString()).updateChildren(updateMap)
                                }else{
                                    labelRef.child(newLabel.toString()).removeValue()
                                }
                            }
                        }
                        exists = true
                    }
                    if (oldLabel > 0 && oldLabel != newLabel){
                        if (snapshot.key == oldLabel.toString()){

                            if (labelFire != null ) {
                                val noteUids = labelFire.noteUids
                                noteUids.remove(noteUid)
                                val updateMap = HashMap<String, Any>()
                                updateMap["noteUids"] = noteUids
                                if (noteUids.isNotEmpty()) {
                                    labelRef.child(oldLabel.toString()).updateChildren(updateMap)
                                } else {
                                    labelRef.child(oldLabel.toString()).removeValue()
                                }
                            }
                        }
                    }
                }
                if (!exists && add){
                    val newLabelMap = HashMap<String, Any>()
                    val noteUids = ArrayList<String>()
                    noteUids.add(noteUid)
                    newLabelMap["noteUids"] = noteUids
                    if (labelTitle!= null){
                        newLabelMap["labelTitle"] = labelTitle
                    }

                    labelRef.child(newLabel.toString()).setValue(newLabelMap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: $error")
            }
    })


    }

    fun deleteNoteFromLabel(label: Int , noteUid :String){
        val labelRef = fUser?.let { database.getReference(it.uid).child("labels")}
        labelRef?.child(label.toString())?.addListenerForSingleValueEvent( object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val labelFire = snapshot.getValue(LabelFire::class.java)
                if (labelFire != null) {
                    val noteUids = labelFire.noteUids
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

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: $error",)
            }
        })
    }


    fun updateLabelColorOrTitle(labelTitle : String, oldLabelColor : String, newLabelColor : String) {

        val oldLabelRef = fUser?.let { database.getReference(it.uid).child("labels").child(oldLabelColor) }
        oldLabelRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val oldLabelObj = s.getValue(LabelFire::class.java)

                fUser?.let { database.getReference(it.uid).child("labels").child(newLabelColor).addListenerForSingleValueEvent( object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingLabel = snapshot.getValue(LabelFire::class.java)
                        if (oldLabelObj != null){

                            if (newLabelColor!= "0"){

                                val noteUids = HashSet<String>()
                                if (existingLabel != null){
                                    noteUids.addAll(existingLabel.noteUids)
                                    noteUids.addAll(oldLabelObj.noteUids)
                                    val existingLabelRef = database.reference.child(it.uid).child("labels").child(newLabelColor)
                                    val updateMap = HashMap<String, Any>()
                                    updateMap["noteUids"] = ArrayList(noteUids)
                                    updateMap["labelTitle"] = labelTitle
                                    existingLabelRef.updateChildren(updateMap)

                                }else{
                                    val newLabelMap = HashMap<String, Any>()
                                    noteUids.addAll(oldLabelObj.noteUids)

                                    newLabelMap["noteUids"] = ArrayList(noteUids)
                                    newLabelMap["labelTitle"] = labelTitle
                                    val newLabelRef = database.reference.child(it.uid).child("labels").child(newLabelColor)
                                    newLabelRef.setValue(newLabelMap)
                                }

                                for ( noteUid in noteUids){
                                    val noteRef = database.getReference(it.uid).child("notes").child(noteUid)
                                    val noteUpdate = HashMap<String, Any>()
                                    noteUpdate["label"] = Integer.parseInt(newLabelColor)
                                    noteRef.updateChildren(noteUpdate)
                                }
                                oldLabelRef.removeValue()
                            }else{
                                val updateMap = HashMap<String, Any>()
                                updateMap["labelTitle"] = labelTitle
                                oldLabelRef.updateChildren(updateMap)
                            }
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }) }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: $error")
            }
        })

    }
}
