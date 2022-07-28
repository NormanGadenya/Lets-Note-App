package com.neuralbit.letsnote.ui.archived

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.repos.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ArchivedViewModel (application : Application): AndroidViewModel(application) {

    var archivedNotes: LiveData<List<Note>>
    var archivedFireNotes = MutableLiveData<List<NoteFire>>()

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

    fun filterArchivedFireList () : LiveData<List<NoteFire>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(archivedFireNotes,){
                filterList(it,textLower)
            }
        }else{
            archivedFireNotes
        }
    }

    private fun filterList(list : List<NoteFire>, text: String?) : List<NoteFire>{
        val newList = ArrayList<NoteFire>()

        return if (text != null) {
            val textLower= text.toLowerCase(Locale.ROOT)
            for ( note in list){

                if(note.title.toLowerCase(Locale.ROOT).contains(textLower) || note.description.toLowerCase(
                        Locale.ROOT).contains(textLower)){
                    newList.add(note)
                }
            }
            newList
        }else{
            list
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