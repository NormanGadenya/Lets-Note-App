package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.repos.NoteFireRepo
import java.util.*

class AllNotesViewModel (application : Application) : AndroidViewModel(application) {
    var otherFireNotesList = MutableLiveData<List<NoteFire>>()
    var pinnedFireNotesList = MutableLiveData<List<NoteFire>>()
    private val noteFireRepo: NoteFireRepo = NoteFireRepo()
    var searchQuery : MutableLiveData<String> = MutableLiveData()


    fun filterOtherFireList () : LiveData<List<NoteFire>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(otherFireNotesList,){
                filterList(it,textLower)
            }
        }else{
            otherFireNotesList
        }
    }

    fun filterPinnedFireList () : LiveData<List<NoteFire>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(pinnedFireNotesList,){
                filterList(it,textLower)
            }
        }else{
            pinnedFireNotesList
        }
    }


    fun getAllFireNotes () :LiveData<List<NoteFire>> {
        return noteFireRepo.getAllNotes()
    }


    private fun filterList(list : List<NoteFire>, text: String?) : List<NoteFire>{
        val newList = ArrayList<NoteFire>()

        return if (text != null) {
            val textLower= text.toLowerCase(Locale.ROOT)
            for ( note in list){

                if(note.title.toLowerCase(Locale.ROOT).contains(textLower) || note.description.toLowerCase(Locale.ROOT).contains(textLower)){
                    newList.add(note)
                }
            }
            newList
        }else{
            list
        }

    }
}