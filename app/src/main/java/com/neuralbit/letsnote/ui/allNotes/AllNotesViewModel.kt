package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.Note
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class AllNotesViewModel (application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    val repo : NoteRepo
    var pinnedNotes: LiveData<List<Note>>
    var searchQuery : MutableLiveData<String> = MutableLiveData()

    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        repo= NoteRepo(dao)
        allNotes = repo.allNotes
        pinnedNotes = repo.pinnedNotes

    }

    fun deleteNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.delete(note)
    }

    fun filterList( ) : LiveData<List<Note>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(allNotes,){
                filterLiveList(it,textLower)
            }
        }else{
            allNotes
        }

    }

    fun filterPinnedList( ) : LiveData<List<Note>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(pinnedNotes,){
                filterLiveList(it,textLower)
            }
        }else{
            pinnedNotes
        }

    }
    private fun filterLiveList(list: List<Note>, text : String? ): List<Note>{
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

}