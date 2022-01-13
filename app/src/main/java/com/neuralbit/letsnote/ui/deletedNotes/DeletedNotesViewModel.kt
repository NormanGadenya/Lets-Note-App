package com.neuralbit.letsnote.ui.deletedNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.repos.NoteRepo
import java.util.ArrayList

class DeletedNotesViewModel (application: Application): AndroidViewModel(application) {

    var deletedNotes: LiveData<List<Note>>
    val repo : NoteRepo
    var searchQuery : MutableLiveData<String> = MutableLiveData()


    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        repo= NoteRepo(dao)
        deletedNotes = repo.deletedNotes

    }
    fun filterList( ) : LiveData<List<Note>> {
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(deletedNotes,){
                filterLiveList(it,textLower)
            }
        }else{
            deletedNotes
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