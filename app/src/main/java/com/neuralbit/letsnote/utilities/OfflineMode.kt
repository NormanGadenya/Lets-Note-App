package com.neuralbit.letsnote.utilities

import android.app.Application
import com.google.firebase.database.FirebaseDatabase


class OfflineMode : Application() {
    //    fun enablePersistence() {
//        super.onCreate()
//
//        // [START rtdb_enable_persistence]
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
//        // [END rtdb_enable_persistence]
//    }
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}