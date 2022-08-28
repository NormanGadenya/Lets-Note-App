package com.neuralbit.letsnote.utilities

import android.app.Application
import com.google.firebase.database.FirebaseDatabase


class OfflineMode : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

    }
}