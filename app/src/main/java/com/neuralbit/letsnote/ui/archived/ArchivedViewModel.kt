package com.neuralbit.letsnote.ui.archived

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.neuralbit.letsnote.firebase.entities.NoteFire
import java.util.*

class ArchivedViewModel (application : Application): AndroidViewModel(application) {

    var useLocalStorage = false
    var archivedFireNotes = MutableLiveData<ArrayList<NoteFire>>()

    var searchQuery : MutableLiveData<String> = MutableLiveData()
    var itemRestoreClicked : MutableLiveData<Boolean> = MutableLiveData()
    var itemDeleteClicked : MutableLiveData<Boolean> = MutableLiveData()
    var notesToRestore : MutableLiveData<NoteFire> = MutableLiveData()


    fun filterArchivedFireList () : LiveData<ArrayList<NoteFire>>{
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

    private fun filterList(list : ArrayList<NoteFire>, text: String?) : ArrayList<NoteFire>{
        val newList = ArrayList<NoteFire>()

        return if (text != null) {
            val textLower= text.lowercase(Locale.ROOT)
            for ( note in list){
                if (note.todoItems.isNotEmpty()){
                    for (todo in note.todoItems){
                        if(note.title.lowercase(Locale.ROOT).contains(textLower) || note.description.lowercase(
                                Locale.ROOT
                            )
                                .contains(textLower) || todo.item.lowercase(Locale.ROOT).contains(textLower) ){
                            if (!newList.contains(note)){
                                newList.add(note)
                            }
                        }
                    }
                }else{
                    if(note.title.lowercase(Locale.ROOT).contains(textLower) || note.description.lowercase(Locale.ROOT).contains(textLower)){
                        newList.add(note)
                    }else{
                        for(tag in note.tags){
                            if(tag.lowercase(Locale.ROOT).contains(text)){
                                newList.add(note)
                                break
                            }
                        }
                    }
                }
            }
            newList
        }else{
            list
        }

    }




}