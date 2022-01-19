package com.neuralbit.letsnote.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.DatabaseConfiguration
import com.google.firebase.storage.FirebaseStorage
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DatabaseBackUp : BroadcastReceiver() {
    val TAG = "tag"

    override fun onReceive(context: Context, intent: Intent) {
        val firebaseUser = intent.getStringExtra("fUserID")
        val database = NoteDatabase.getDatabase(context)
        if (firebaseUser != null){

//            val dbPath = context.resources.getString(R.string.dbFileLoc)
//            val dbPathShm = context.resources.getString(R.string.dbShmFileLoc)
//            val dbPathWal = context.resources.getString(R.string.dbWalFileLoc)
//
//            val dbFile: Uri = Uri.fromFile(File(dbPath))
//            val dbShmFile = Uri.fromFile(File(dbPathShm))
//            val dbWalFile = Uri.fromFile(File(dbPathWal))
//            val firebaseStorage = FirebaseStorage.getInstance()
//            val mStorageReference = firebaseUser.let { firebaseStorage.reference.child(it) }
//            val dbFileRef = mStorageReference.child("letsNoteDB1")
//            val dbShmFileRef = mStorageReference.child("letsNoteDB1-shm")
//            val dbWalFileRef = mStorageReference.child("letsNoteDB1-wal")
//            GlobalScope.launch {
//                withContext(Dispatchers.IO){
//                    dbFileRef.putFile(dbFile)
//                    dbShmFileRef.putFile(dbShmFile)
//                    dbWalFileRef.putFile(dbWalFile)
//                }
//            }

        }
    }
}