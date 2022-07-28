package com.neuralbit.letsnote.ui.tag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.repos.*

class TagViewModel (application: Application): AndroidViewModel(application) {
    private val noteTagRepo : NoteTagRepo
    private val tagRepo : TagRepo
    private val noteRepo : NoteRepo
    private val reminderRepo : ReminderRepo
    var allTags  = ArrayList<Tag>()
    private val labelRepo : LabelRepo
    var searchQuery : MutableLiveData <String>
    init {
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        val noteDao = NoteDatabase.getDatabase(application).getNotesDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()
        val reminderDao = NoteDatabase.getDatabase(application).getReminderDao()
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()
        noteTagRepo = NoteTagRepo(noteTagDao)
        tagRepo = TagRepo(tagDao)
        noteRepo = NoteRepo(noteDao)
        reminderRepo = ReminderRepo(reminderDao)
        labelRepo = LabelRepo(labelDao)
        searchQuery = MutableLiveData()
    }

}