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
    var otherFireNotesList = MutableLiveData<LinkedList<NoteFire>>()
    var pinnedFireNotesList = MutableLiveData<LinkedList<NoteFire>>()
    var allFireNotes = MutableLiveData<ArrayList<NoteFire>>()
    private val noteFireRepo: NoteFireRepo = NoteFireRepo()
    var searchQuery : MutableLiveData<String> = MutableLiveData()
    var itemSelectEnabled : MutableLiveData<Boolean> = MutableLiveData()
    var notesToDelete : MutableLiveData<NoteFire> = MutableLiveData()
    var itemDeleteClicked : MutableLiveData<Boolean> = MutableLiveData()
    var itemArchiveClicked : MutableLiveData<Boolean> = MutableLiveData()
    var staggeredView : MutableLiveData<Boolean> = MutableLiveData()
    var selectedNotes = HashSet<NoteFire>()
    var deleteFrag : MutableLiveData<Boolean> = MutableLiveData()
    var archiveFrag = false


    fun filterOtherFireList () : LiveData<LinkedList<NoteFire>>{
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

    fun filterPinnedFireList () : LiveData<LinkedList<NoteFire>>{
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


    suspend fun getAllFireNotes () : LiveData<ArrayList<NoteFire>>{
        return noteFireRepo.getAllNotes()
    }

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) {
        noteFireRepo.updateNote(noteUpdate,noteUid)
    }

    private fun filterList(list : LinkedList<NoteFire>, text: String?) : LinkedList<NoteFire>{
        val newList = LinkedList<NoteFire>()

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
                    if(note.title.lowercase(Locale.ROOT).contains(textLower) || note.description.lowercase(
                            Locale.ROOT
                        )
                            .contains(textLower)){
                        newList.add(note)
                    }
                }
            }
            newList
        }else{
            list
        }

    }

}