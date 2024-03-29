package com.neuralbit.letsnote.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.neuralbit.letsnote.firebase.repos.LabelFireRepo
import com.neuralbit.letsnote.firebase.repos.NoteFireRepo
import com.neuralbit.letsnote.firebase.repos.TagFireRepo

class DeleteReceiver : BroadcastReceiver() {

    private val noteRepo = NoteFireRepo()
    private val tagRepo = TagFireRepo()
    private val labelRepo = LabelFireRepo()

    override fun onReceive(context: Context?, intent: Intent?) {
        val noteUid = intent?.getStringExtra("noteUid")
        val labelColor = intent?.getIntExtra("labelColor",0)
        val timeStamp = intent?.getLongExtra("timeStamp",0)
        val tagList = intent?.getStringArrayListExtra("tagList")

        if (timeStamp != null && noteUid != null){
            noteRepo.deleteNote(noteUid)
            if (tagList != null){
                tagRepo.deleteNoteFromTags(tagList,noteUid)
            }
            if (labelColor != null){
                labelRepo.deleteNoteFromLabel(labelColor,noteUid)
            }
        }
    }
}