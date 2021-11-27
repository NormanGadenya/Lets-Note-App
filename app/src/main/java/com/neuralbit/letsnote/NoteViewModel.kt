package com.neuralbit.letsnote

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    var allTags: LiveData<List<Tag>>
    val TAG = "NoteViewModel"
    val repo : NoteRepo
    val tagRepo : TagRepo
    var texChange = false
    var texChanged = MutableLiveData<Boolean>()
    var deleted = false
    var delete = MutableLiveData<Boolean>()
    var archived = false
    var archive = MutableLiveData<Boolean>()
    var pinned = false
    var pin = MutableLiveData<Boolean>()
    lateinit var list : List<Note>
    var notes : List<Note> = listOf()
    var searchQurery : MutableLiveData<String>
    var archivedNote : LiveData<List<Note>>
    var pinnedNotes : LiveData<List<Note>>
    var wordStart : MutableLiveData<Int>
    var wordEnd : MutableLiveData<Int>
    var noteDescString : MutableLiveData<String>
    var newTagTyped : MutableLiveData<Boolean>
    var backPressed : MutableLiveData<Boolean>

    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()
        repo= NoteRepo(dao)
        tagRepo = TagRepo(tagDao)
        allNotes = repo.allNotes
        archivedNote = repo.archivedNotes
        pinnedNotes = repo.pinnedNotes
        allTags = tagRepo.allTags
        searchQurery = MutableLiveData<String>()
        wordStart = MutableLiveData()
        wordEnd = MutableLiveData()
        noteDescString = MutableLiveData()
        newTagTyped = MutableLiveData()
        backPressed = MutableLiveData()
    }

    fun getTagString(text: String){

        noteDescString.value = text.substring(wordStart.value!!, wordEnd.value!!)


    }


    fun noteChanged (b : Boolean){
        texChange = b
        texChanged.value= texChange
    }

    fun filterList( ) : LiveData<List<Tag>>{
        val textLower = noteDescString.value
        Log.d(TAG, "filterList: ${noteDescString.value}")
        return Transformations.map(allTags,){
            filterLiveList(it,textLower)
        }
    }
    private fun filterLiveList(list: List<Tag>, text : String? ): List<Tag>{
        var newList = ArrayList<Tag>()

        return if(text!=null){
            var textLower= text.toLowerCase()
            for ( tag in list){

                if(tag.tagTitle.toLowerCase().contains(textLower)  ){
                    newList.add(tag)

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

    fun Pinned(b : Boolean){
        pinned = b
        pin.value = pinned
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

    fun removeArchive(id:ArchivedNote) = viewModelScope.launch(Dispatchers.IO){
        repo.deleteArchive(id)
    }

    fun pinNote(id:PinnedNote) = viewModelScope.launch(Dispatchers.IO){
        repo.insertPinned(id)
    }

    fun removePin(id:PinnedNote) = viewModelScope.launch(Dispatchers.IO){
        repo.deletePinned(id)
    }

    fun addTag(tag : Tag) = viewModelScope.launch(Dispatchers.IO){
        tagRepo.insert(tag)
    }

    fun deleteTag(tag : Tag) = viewModelScope.launch(Dispatchers.IO){
        tagRepo.delete(tag)
    }

}