package com.neuralbit.letsnote.ui.tag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.Tag
import com.neuralbit.letsnote.relationships.NotesWithTag
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.repos.NoteRepo
import com.neuralbit.letsnote.repos.NoteTagRepo
import com.neuralbit.letsnote.repos.TagRepo

class TagViewModel (application: Application): AndroidViewModel(application) {
    var allTags: LiveData<List<Tag>>
    val noteTagRepo : NoteTagRepo
    val tagRepo : TagRepo

    init {
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()
        noteTagRepo = NoteTagRepo(noteTagDao)
        tagRepo = TagRepo(tagDao)
        allTags = tagRepo.allTags
    }
    suspend fun getNotesWithTag(tagTitle : String) : List<NotesWithTag> {
        return noteTagRepo.getNotesWithTag(tagTitle)
    }

    suspend fun getTagsWithNote(noteID: Long):List<TagsWithNote> {
        return noteTagRepo.getTagsWithNote(noteID)
    }
}