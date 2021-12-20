package com.neuralbit.letsnote

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.databinding.FragmentAllNotesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    var allTags: LiveData<List<Tag>>
    val TAG = "NoteViewModel"
    val repo : NoteRepo
    val tagRepo : TagRepo
    val noteTagRepo : NoteTagRepo
    var texChange = false
    var texChanged = MutableLiveData<Boolean>()
    var deleted = MutableLiveData<Boolean>()
    var archived = MutableLiveData<Boolean>()
    var pinned = MutableLiveData<Boolean>()
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
    var tagList : ArrayList<Tag>

    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        repo= NoteRepo(dao)
        tagRepo = TagRepo(tagDao)
        noteTagRepo = NoteTagRepo(noteTagDao)
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
        tagList = ArrayList<Tag>()
        pinned = MutableLiveData()
        archived = MutableLiveData()
        deleted = MutableLiveData()
    }

    fun getTagString(text: String){
        noteDescString.value = text.substring(wordStart.value!!, wordEnd.value!!)

    }


    fun addTagToList(tag : Tag) {

        if (!tagList.contains(tag)){
            tagList.add(tag)

        }
    }


    fun noteChanged (b : Boolean){
        texChange = b
        texChanged.value= texChange
    }

    fun filterList( ) : LiveData<List<Tag>>  {
        val textLower = noteDescString.value
        Log.d(TAG, "filterList: ${noteDescString.value}")
        return Transformations.map(allTags,){
            filterLiveList(it,textLower)
        }
    }
    private fun filterLiveList(list: List<Tag>, text : String? ): List<Tag>{
        val newList = ArrayList<Tag>()

        return if(text!=null){
            val textLower= text.toLowerCase()
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

    fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef) = viewModelScope.launch(Dispatchers.IO){
        noteTagRepo.insertNoteTagCrossRef(crossRef)
    }
    fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef) = viewModelScope.launch(Dispatchers.IO){
        noteTagRepo.deleteNoteTagCrossRef(crossRef)
    }

    fun getNotesWithTag(tagTitle : String) = viewModelScope.launch(Dispatchers.IO){
        noteTagRepo.getNotesWithTag(tagTitle)
    }

     suspend fun getTagsWithNote(noteID : Int):List<TagsWithNote> {
        return noteTagRepo.getTagsWithNote(noteID)
    }



}