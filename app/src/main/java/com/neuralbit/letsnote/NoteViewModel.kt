package com.neuralbit.letsnote

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    val TAG = "NoteViewModel"
    val repo : NoteRepo
    var texChange = false
    var texChanged = MutableLiveData<Boolean>()
    var deleted = false
    var delete = MutableLiveData<Boolean>()
    var archived = false
    var archive = MutableLiveData<Boolean>()
    lateinit var list : List<Note>
    var notes : List<Note> = listOf()
    var searchQurery : MutableLiveData<String> = MutableLiveData()
    var archivedNote : LiveData<List<Note>>
    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        repo= NoteRepo(dao)
        allNotes = repo.allNotes
        archivedNote = repo.archivedNotes


    }

    fun TextChanged (b : Boolean){
        texChange = b
        texChanged.value= texChange
    }

    fun filterList(  ) : LiveData<List<Note>>{
        val textLower = searchQurery.value

        var list : List<Note>


        return Transformations.map(allNotes,){
            filterLiveList(it,textLower)
        }
    }
    fun filterLiveList(list: List<Note>, text : String? ): List<Note>{
        var newList = ArrayList<Note>()

        return if(text!=null){
            var textLower= text.toLowerCase()
            for ( note in list){

                if(note.title.toLowerCase().contains(textLower) || note.description.toLowerCase().contains(textLower) ){
                    newList.add(note)

                }
            }

            newList
        }else{
            list
        }


    }

    fun Delete(b : Boolean){
        deleted= b
        delete.value = deleted
    }
    fun Archive(b : Boolean){
        archived = b
        archive.value = archived
    }

    fun deleteNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.delete(note)
    }
    fun updateNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.update(note)
    }fun addNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.insert(note)
    }
    fun archiveNote(id:ArchivedNote) = viewModelScope.launch(Dispatchers.IO){
        repo.insertArchive(id)
    }
}