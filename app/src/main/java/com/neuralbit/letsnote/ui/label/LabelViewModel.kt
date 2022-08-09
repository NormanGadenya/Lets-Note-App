package com.neuralbit.letsnote.ui.label

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.entities.DeletedNote
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.relationships.LabelWIthNotes
import com.neuralbit.letsnote.repos.LabelRepo
import com.neuralbit.letsnote.repos.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LabelViewModel (application: Application): AndroidViewModel(application)  {
    private val labelRepo : LabelRepo
    private val noteRepo : NoteRepo
    init {
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()
        labelRepo = LabelRepo(labelDao)
        val noteDao = NoteDatabase.getDatabase(application).getNotesDao()
        noteRepo = NoteRepo(noteDao)
    }

    fun getNotesWithLabel ( labelID : Int) : LiveData<List<LabelWIthNotes>>{
        return labelRepo.getNotesWithLabel(labelID)
    }

    fun getNoteLabel( noteID : Long) : LiveData<Label> {
        return labelRepo.getNoteLabel(noteID)
    }
    fun getAllNotes() : LiveData<List<LabelWIthNotes>> {
        return labelRepo.getAllNotes()
    }
    fun deleteLabel(labelID: Int) = viewModelScope.launch(Dispatchers.IO){
        labelRepo.deleteLabel(labelID)
    }
    fun getDeletedNote(noteID: Long):LiveData<DeletedNote> {
        return noteRepo.getDeletedNote(noteID)
    }
}