package com.neuralbit.letsnote.utilities

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.neuralbit.letsnote.AddEditNoteActivity

class AlertReceiver : BroadcastReceiver() {
    val TAG = "tag"
    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra("noteTitle")
        val noteDesc = intent.getStringExtra("noteDesc")
        val notificationHelper = NotificationHelper(context,noteTitle,noteDesc)
        val noteType = intent.getStringExtra("noteType")
        val noteUid = intent.getStringExtra("noteUid")
        val timeStamp = intent.getLongExtra("timeStamp",0)
        val label = intent.getIntExtra("labelColor",0)
        val tagList = intent.getStringArrayListExtra("tagList")
        val pinned = intent.getBooleanExtra("pinned",false)
        val archived = intent.getBooleanExtra("archieved",false)
        val todoItems = intent.getStringExtra("todoItems")

        val nb = notificationHelper.channelNotification

        val i = Intent(context,AddEditNoteActivity::class.java)
        i.putExtra("noteType",noteType)
        i.putExtra("noteType","Edit")
        i.putExtra("noteTitle",noteTitle)
        i.putExtra("noteDescription",noteDesc)
        i.putExtra("noteUid",noteUid)
        i.putExtra("timeStamp",timeStamp)
        i.putExtra("labelColor",label)
        i.putExtra("pinned",pinned)
        i.putExtra("archieved",archived)
        i.putExtra("todoItems",todoItems)
        i.putStringArrayListExtra("tagList", tagList)
        val pendingIntent = PendingIntent.getActivity(context, timeStamp.toInt(), i, PendingIntent.FLAG_IMMUTABLE)
        nb.setContentIntent(pendingIntent)
        notificationHelper.manager.notify(timeStamp.toInt(), nb.build())
    }
}