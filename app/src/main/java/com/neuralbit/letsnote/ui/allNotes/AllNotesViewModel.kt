package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import androidx.lifecycle.*
import com.neuralbit.letsnote.Note
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllNotesViewModel (application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    val repo : NoteRepo
    var searchQurery : MutableLiveData<String> = MutableLiveData()

    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        repo= NoteRepo(dao)
        allNotes = repo.allNotes

    }

    fun deleteNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.delete(note)
    }
}