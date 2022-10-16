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
import com.neuralbit.letsnote.firebase.entities.TagFire

class TagFireRepo {

    private val database = Firebase.database
    val TAG = "TagFireRepo"

    private var fUser = FirebaseAuth.getInstance().currentUser

    fun getAllTags () : LiveData<List<TagFire>> {
        val live = MutableLiveData<List<TagFire>>()
        var tagRef = fUser?.let { database.getReference(it.uid).child("tags") }

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val tags = ArrayList<TagFire>()
                for ( s : DataSnapshot in snapshot.children ){
                    Log.d(TAG, "onDataChange: $s")
                    val tag = s.getValue(TagFire::class.java)
                    if (tag != null) {
                        tag.tagName = "#"+ s.key!!
                        tags.add(tag)
                    }
                }
                Log.d(TAG, "onDataChange: $tags")
                live.value = tags
            }

            override fun onCancelled(error: DatabaseError) {
                throw error.toException()

            }

        }
        tagRef?.addValueEventListener(eventListener)
        FirebaseAuth.getInstance().addAuthStateListener {
            tagRef?.removeEventListener(eventListener)
            fUser= it.currentUser
            tagRef = it.currentUser?.uid?.let { it1 -> database.getReference(it1).child("tags") }

            tagRef?.addValueEventListener(eventListener)

        }
        return live
    }


    fun addOrDeleteTags(newTagsAdded: HashSet<String>, deletedTags: HashSet<String>, noteUid: String) {
        val tagRef = fUser?.let { database.getReference(it.uid).child("tags")}
        for (tag in newTagsAdded) {
            var tagStr = tag
            val split = tagStr.split("#")
            if (split.size > 1){
                tagStr = split[1]
            }
            tagRef?.child(tagStr)?.addListenerForSingleValueEvent( object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val tagFire = snapshot.getValue(TagFire::class.java)
                        if (tagFire != null) {
                            val noteUids = tagFire.noteUids
                            noteUids.add(noteUid)
                            val updateMap = HashMap<String, Any>()
                            updateMap["noteUids"] = noteUids
                            tagRef.child(tagStr).updateChildren(updateMap)

                        }

                    }else{
                        val newTagMap = HashMap<String, Any>()
                        val noteUids = ArrayList<String>()
                        noteUids.add(noteUid)
                        newTagMap["noteUids"] = noteUids
                        tagRef.child(tagStr).setValue(newTagMap)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    throw error.toException()
                }
            })
        }

        for ( tag in deletedTags){

            var tagStr = tag
            val split = tagStr.split("#")
            if (split.size > 1){
                tagStr = split[1]
            }
            if (!tagStr.contains("#")){
                tagRef?.child(tagStr)?.addListenerForSingleValueEvent( object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val tagFire = snapshot.getValue(TagFire::class.java)
                        if (tagFire != null) {
                            val noteUids = tagFire.noteUids
                            noteUids.remove(noteUid)
                            val updateMap = HashMap<String, Any>()
                            updateMap["noteUids"] = noteUids
                            if (noteUids.isNotEmpty()){
                                tagRef.child(tagStr).updateChildren(updateMap)
                            }else{
                                tagRef.child(tagStr).removeValue()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "onCancelled: $error", )
                    }
                })
            }

        }

    }

    fun deleteNoteFromTags(tags : List<String>, noteUid: String){
        val tagRef = fUser?.let { database.getReference(it.uid).child("tags") }
        for ( tag in tags){

            var tagStr = tag
            val split = tagStr.split("#")
            if (split.size > 1){
                tagStr = split[1]
            }
            tagRef?.child(tagStr)?.addListenerForSingleValueEvent( object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    val tagFire = snapshot.getValue(TagFire::class.java)
                    if (tagFire != null) {
                        val noteUids = tagFire.noteUids
                        noteUids.remove(noteUid)
                        val updateMap = HashMap<String, Any>()
                        updateMap["noteUids"] = noteUids
                        if (noteUids.isNotEmpty()){
                            tagRef.child(tagStr).updateChildren(updateMap)
                        }else{
                            tagRef.child(tagStr).removeValue()
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: $error", )
                }
            })

        }
    }

}