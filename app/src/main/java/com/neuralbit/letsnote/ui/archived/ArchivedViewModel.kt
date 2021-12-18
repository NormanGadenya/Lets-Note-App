package com.neuralbit.letsnote.ui.archived

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.Note
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class ArchivedViewModel (application : Application): AndroidViewModel(application) {

    var archivedNotes: LiveData<List<Note>>
    val repo : NoteRepo
    var searchQuery : MutableLiveData<String> = MutableLiveData()


    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        repo= NoteRepo(dao)
        archivedNotes = repo.archivedNotes

    }

    fun deleteNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        repo.delete(note)
    }

    fun filterList( ) : LiveData<List<Note>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(archivedNotes,){
                filterLiveList(it,textLower)
            }
        }else{
            archivedNotes
        }

    }
    private fun filterLiveList(list: List<Note>, text : String? ): List<Note>{
        var newList = ArrayList<Note>()

        return if(text!=null){
            var textLower= text.toLowerCase()
            for ( note in list){

                if(note.title?.toLowerCase()?.contains(textLower) == true || note.description?.toLowerCase()
                        ?.contains(textLower) == true
                ){
                    newList.add(note)
                }
            }

            newList
        }else{
            list
        }


    }

}