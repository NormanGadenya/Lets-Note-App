package com.neuralbit.letsnote.receivers



import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.firebase.entities.TodoItem
import com.neuralbit.letsnote.firebase.repos.NoteFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.repos.NoteRoomRepo
import com.neuralbit.letsnote.room.repos.NoteTagRoomRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RescheduleAlarmsReceiver : BroadcastReceiver() {
    val TAG = "RescheduleAlarmsReceiver"

    private val fUser = FirebaseAuth.getInstance().currentUser
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
        val noteRoomDao = p0?.applicationContext?.let { NoteDatabase.getDatabase(it).getNotesDao() }
        val noteRoomRepo = noteRoomDao?.let { NoteRoomRepo(it) }

        val noteTagRoomDao = p0?.applicationContext?.let { NoteDatabase.getDatabase(it).getNoteTagDao() }
        val noteTagRoomRepo = noteTagRoomDao?.let { NoteTagRoomRepo(it) }
        if ( p1?.action == Intent.ACTION_BOOT_COMPLETED ){
            if (fUser != null){
                noteRepo.getAllNotes().value?.forEach { noteFire ->

                    if (!noteFire.archived && noteFire.deletedDate==(0).toLong() && noteFire.reminderDate> System.currentTimeMillis()){
                        if (p0 != null) {
                            startAlarm(noteFire, noteFire.reminderDate.toInt(),p0)
                        }
                    }
                }
            }else{
                if (noteRoomRepo != null && noteTagRoomRepo != null){
                    GlobalScope.launch(Dispatchers.IO) {
                        for (note in noteRoomRepo.getAllNotes()) {
                            val noteFire = NoteFire()
                            noteFire.noteUid = note.noteUid
                            noteFire.description = note.description!!
                            noteFire.title = note.title!!
                            noteFire.timeStamp = note.timestamp
                            noteFire.label = note.labelColor
                            noteFire.protected = note.locked
                            noteFire.archived = note.archived
                            noteFire.pinned = note.pinned
                            noteFire.deletedDate = note.deletedDate
                            noteFire.reminderDate = note.reminderDate
                            val tagsList = java.util.ArrayList<String>()
                            for (tagsWithNote in noteTagRoomRepo.getTagsWithNote(note.noteUid)) {
                                for (t in tagsWithNote.tags){
                                    tagsList.add("#${t.tagTitle}")
                                }
                            }
                            noteFire.tags = tagsList
                            val todoItems = noteRoomRepo.getTodoList(note.noteUid)
                            val items = todoItems.map { t -> TodoItem(item = t.itemDesc, checked = t.itemChecked) }
                            noteFire.todoItems = items

                            if (!noteFire.archived && noteFire.deletedDate==(0).toLong() && noteFire.reminderDate> System.currentTimeMillis()){
                                startAlarm(noteFire, noteFire.reminderDate.toInt(),p0)
                            }

                        }
                    }
                }
            }
        }


    }
}