package com.neuralbit.letsnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.entities.LabelFire
import com.neuralbit.letsnote.entities.NoteFireIns
import com.neuralbit.letsnote.entities.TagFire
import com.neuralbit.letsnote.repos.LabelFireRepo
import com.neuralbit.letsnote.repos.NoteFireRepo
import com.neuralbit.letsnote.repos.TagFireRepo

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    val TAG = "NoteViewModel"
    private val noteFireRepo : NoteFireRepo
    private val labelFireRepo : LabelFireRepo
    private val tagFireRepo : TagFireRepo
    var oldTagList = ArrayList<String>()
    var newTags = HashSet<String>()
    var deletedTags = HashSet<String>()
    var noteChanged = MutableLiveData<Boolean>()
    var deleted = MutableLiveData<Boolean>()
    var archived : MutableLiveData<Boolean>
    var deletedNote : MutableLiveData<Boolean>
    var labelChanged : Boolean = false
    var labelColor : Int = 0
    var pinned : MutableLiveData<Boolean>
    var reminderSet : MutableLiveData<Boolean>
    var labelSet : MutableLiveData<Boolean>
    var reminderTime : Long = 0
    var searchQuery : MutableLiveData<String>
    var noteDescString : MutableLiveData<String>
    var newTagTyped : MutableLiveData<Boolean>
    var backPressed : MutableLiveData<Boolean>
    var undoMode : MutableLiveData<Boolean>

    init{
        noteFireRepo = NoteFireRepo()
        tagFireRepo = TagFireRepo()
        labelFireRepo = LabelFireRepo()
        searchQuery = MutableLiveData<String>()
        noteDescString = MutableLiveData()
        newTagTyped = MutableLiveData()
        backPressed = MutableLiveData()
        pinned = MutableLiveData()
        archived = MutableLiveData()
        deletedNote = MutableLiveData()
        reminderSet = MutableLiveData()
        labelSet = MutableLiveData()
        deleted = MutableLiveData()
        undoMode = MutableLiveData()

    }

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) {
        noteFireRepo.updateNote(noteUpdate,noteUid)
    }

    fun addFireNote(note: NoteFireIns) : String ?{
        return noteFireRepo.addNote(note)
    }


    fun allFireTags() : LiveData<List<TagFire>>{
        return tagFireRepo.getAllTags()
    }

    fun allFireLabels() : LiveData<List<LabelFire>>{
        return labelFireRepo.getAllLabels()
    }

    fun addOrDeleteLabel( labelColor : Int, noteUid: String, add : Boolean){
        labelFireRepo.addOrDeleteLabels(labelColor,noteUid,add)
    }

    fun addOrDeleteTags(newTagsAdded: HashSet<String>, deletedTags: HashSet<String>, noteUid: String) {
        tagFireRepo.addOrDeleteTags(newTagsAdded,deletedTags,noteUid)
    }


}