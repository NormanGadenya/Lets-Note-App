package com.neuralbit.letsnote.utilities



import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RescheduleAlarmsService : BroadcastReceiver() {
    private val noteRepo = NoteFireRepo()

    private fun startAlarm(note: NoteFire, requestCode: Int, context: Context) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        intent.putExtra("noteTitle", note.title)
        intent.putExtra("noteUid", note.noteUid)
        intent.putExtra("noteDesc", note.description)
        intent.putExtra("timeStamp", note.timeStamp)
        intent.putExtra("labelColor", note.label)
        intent.putExtra("pinned", note.pinned)
        intent.putExtra("archieved", false)
        intent.putExtra("protected", note.protected)
        val tags = ArrayList(note.tags)
        intent.putStringArrayListExtra("tagList", tags)
        intent.putExtra("noteType", "Edit")
        val pendingIntent =
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            note.reminderDate,
            pendingIntent
        )
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        GlobalScope.launch {
            noteRepo.getAllNotes().value?.forEach { noteFire ->
                if (!noteFire.archived && noteFire.deletedDate==(0).toLong() && noteFire.reminderDate> System.currentTimeMillis()){
                    if (p0 != null) {
                        startAlarm(noteFire, noteFire.reminderDate.toInt(),p0)
                    }
                }
            }
        }
    }
}