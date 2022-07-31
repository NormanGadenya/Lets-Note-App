package com.neuralbit.letsnote.Services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.neuralbit.letsnote.repos.LabelFireRepo
import com.neuralbit.letsnote.repos.NoteFireRepo
import com.neuralbit.letsnote.repos.TagFireRepo

class DeleteReceiver : BroadcastReceiver() {

    private val noteRepo = NoteFireRepo()
    private val tagRepo = TagFireRepo()
    private val labelRepo = LabelFireRepo()

    override fun onReceive(context: Context?, intent: Intent?) {
        val noteUid = intent?.getStringExtra("noteUid")
        val labelColor = intent?.getIntExtra("labelColor",0)
        val timeStamp = intent?.getLongExtra("timeStamp",0)
        val tagList = intent?.getStringArrayListExtra("tagList")
        val pref = context?.getSharedPreferences("DeletedNotes", AppCompatActivity.MODE_PRIVATE)
        val editor: SharedPreferences.Editor ?= pref?.edit()
        val noteUids = pref?.getStringSet("noteUids", HashSet())
        val deletedNoteUids = HashSet<String>()
        if (noteUids != null){ deletedNoteUids.addAll(noteUids)}
        deletedNoteUids.remove(noteUid)
        editor?.putStringSet("noteUids",deletedNoteUids)
        editor?.apply()

        if (timeStamp != null && noteUid != null){
            val noteList = ArrayList<String>()
            noteList.add(noteUid)
            noteRepo.deleteNote(noteList)
            if (tagList != null){
                tagRepo.deleteNoteFromTags(tagList,noteList)
            }
            if (labelColor != null){
                labelRepo.deleteNotesFromLabel(labelColor,noteList)
            }
        }
    }
}