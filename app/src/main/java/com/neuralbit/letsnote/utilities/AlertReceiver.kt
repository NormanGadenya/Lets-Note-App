package com.neuralbit.letsnote.utilities

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.utilities.NotificationHelper

class AlertReceiver : BroadcastReceiver() {
    val TAG = "tag"
    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra("noteTitle")
        val notificationHelper = NotificationHelper(context,noteTitle)
//        val noteType = intent.getStringExtra("noteType")
        val noteID = intent.getLongExtra("noteID", -1)
        val nb = notificationHelper.channelNotification
        Log.d(TAG, "onReceive: $noteTitle ")
        val i = Intent(context, AddEditNoteActivity::class.java)
//
//        i.putExtra("noteType",noteType)
//        i.putExtra("noteID",noteID)
        val pendingIntent = PendingIntent.getActivity(context, 1, i, PendingIntent.FLAG_ONE_SHOT)
        nb.setContentIntent(pendingIntent)
        notificationHelper.manager.notify(1, nb.build())
    }
}