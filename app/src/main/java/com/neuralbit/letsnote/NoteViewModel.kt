package com.neuralbit.letsnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    val allNotes: LiveData<List<Note>>
    val repo : NoteRepo
    var texChange = false
    var texChanged = MutableLiveData<Boolean>()
    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        repo= NoteRepo(dao)
        allNotes = repo.allNotes

    }

    fun TextChanged (b : Boolean){
        texChange = b
        texChanged.value= texChange
    }
    fun deleteNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.delete(note)
    }
    fun updateNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.update(note)
    }fun addNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.insert(note)
    }
}