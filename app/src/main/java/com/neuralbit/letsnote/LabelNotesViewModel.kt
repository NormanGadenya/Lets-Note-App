package com.neuralbit.letsnote

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.relationships.LabelWIthNotes
import com.neuralbit.letsnote.repos.LabelRepo

class LabelNotesViewModel(
    application: Application) :AndroidViewModel(application){
    val searchQuery: MutableLiveData<String>
    private val labelRepo  : LabelRepo

    init {
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()
        labelRepo = LabelRepo(labelDao)
        searchQuery = MutableLiveData()
    }

    fun getNotesWithLabel ( labelID : Int) : LiveData<List<LabelWIthNotes>> {
        return labelRepo.getNotesWithLabel(labelID)
    }

    fun getNoteLabel( noteID : Long) : LiveData<Label>{
        return labelRepo.getNoteLabel(noteID)
    }
}