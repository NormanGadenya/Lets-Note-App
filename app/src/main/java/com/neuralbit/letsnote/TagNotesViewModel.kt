package com.neuralbit.letsnote

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.relationships.LabelWIthNotes
import com.neuralbit.letsnote.relationships.NotesWithTag
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.repos.LabelRepo
import com.neuralbit.letsnote.repos.NoteTagRepo
import com.neuralbit.letsnote.repos.TagRepo

class TagNotesViewModel(
    application: Application) :AndroidViewModel(application){
    val searchQuery: MutableLiveData<String>
    private val noteTagRepo  : NoteTagRepo

    init {
        val tagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        noteTagRepo = NoteTagRepo(tagDao)
        searchQuery = MutableLiveData()
    }

    suspend fun getNotesWithTag ( tagTitle: String) : List<NotesWithTag> {
        return noteTagRepo.getNotesWithTag(tagTitle)
    }

    suspend fun getTagWithNote( noteID : Long) : List<TagsWithNote>{
        return noteTagRepo.getTagsWithNote(noteID)
    }
}