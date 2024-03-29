package com.neuralbit.letsnote.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.neuralbit.letsnote.ui.addEditNote.AddEditNoteActivity
import com.neuralbit.letsnote.ui.addEditNote.Fingerprint
import com.neuralbit.letsnote.utilities.NotificationHelper

class AlertReceiver : BroadcastReceiver() {
    val TAG = "tag"
    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra("noteTitle")
        val noteDesc = intent.getStringExtra("noteDesc")
        val protected = intent.getBooleanExtra("protected",false)
        val notificationHelper = NotificationHelper(context,noteTitle,noteDesc,protected)
        val noteType = intent.getStringExtra("noteType")
        val noteUid = intent.getStringExtra("noteUid")
        val timeStamp = intent.getLongExtra("timeStamp",0)
        val label = intent.getIntExtra("labelColor",0)
        val tagList = intent.getStringArrayListExtra("tagList")
        val pinned = intent.getBooleanExtra("pinned",false)
        val archived = intent.getBooleanExtra("archieved",false)
        val todoItems = intent.getStringExtra("todoItems")

        val nb = notificationHelper.channelNotification

        val i : Intent = if (protected){
            Intent(context, Fingerprint::class.java)
        }else{
            Intent(context, AddEditNoteActivity::class.java)
        }
        i.putExtra("noteType",noteType)
        i.putExtra("noteType","Edit")
        i.putExtra("noteTitle",noteTitle)
        i.putExtra("noteDescription",noteDesc)
        i.putExtra("noteUid",noteUid)
        i.putExtra("timeStamp",timeStamp)
        i.putExtra("labelColor",label)
        i.putExtra("pinned",pinned)
        i.putExtra("protected", protected)
        i.putExtra("archieved",archived)
        i.putExtra("todoItems",todoItems)
        i.putExtra("protected",protected)
        i.putStringArrayListExtra("tagList", tagList)
        val pendingIntent = PendingIntent.getActivity(context, timeStamp.toInt(), i, PendingIntent.FLAG_IMMUTABLE)
        nb.setContentIntent(pendingIntent)
        notificationHelper.manager.notify(timeStamp.toInt(), nb.build())
    }
}