package com.neuralbit.letsnote.Services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neuralbit.letsnote.MainActivity
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.R
import java.io.File
import java.io.IOException

class DownloadDBService : Service(){

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
//    private fun downloadDB(mStorageReference: StorageReference) {
//        try {
//
//
//            val dbFileRef = mStorageReference.child("letsNoteDB1")
//            val dbShmFileRef = mStorageReference.child("letsNoteDB1-shm")
//            val dbWalFileRef = mStorageReference.child("letsNoteDB1-wal")
//
//            dbWalFileRef.downloadUrl.addOnFailureListener{
//                val intent = Intent(this@SignInActivity, MainActivity::class.java)
//                startActivity(intent)
//            }
//            val database = NoteDatabase.getDatabase(application)
//            database.close()
//
//            mStorageReference.downloadUrl.addOnSuccessListener{
//                val dir = File(getString(R.string.databaseLoc))
//                if (dir.isDirectory) {
//                    val children = dir.list()
//                    if (children!=null){
//                        for (i in children.indices) {
//                            File(dir, children[i]).delete()
//                        }
//                    }
//
//                }
//                val dbPath = resources.getString(R.string.dbFileLoc)
//                val dbPathShm = resources.getString(R.string.dbShmFileLoc)
//                val dbPathWal = resources.getString(R.string.dbWalFileLoc)
//                dbFileRef.getFile(Uri.fromFile(File(dbPath))).addOnCompleteListener{
//                    dbShmFileRef.getFile(Uri.fromFile(File(dbPathShm))).addOnCompleteListener{
//
//                    }.addOnCompleteListener{
//                        dbWalFileRef.getFile(Uri.fromFile(File(dbPathWal))).addOnCompleteListener {
//                            val intent = Intent(this@SignInActivity, MainActivity::class.java)
//                            startActivity(intent)
//                        }
//
//                    }
//                }
//
//
//                val intent = Intent(applicationContext, MainActivity::class.java)
//                startActivity(intent)
//            }
//            dbFileRef.downloadUrl.addOnSuccessListener {
//
//
//
//            }
//
//
//
//
//
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }

}