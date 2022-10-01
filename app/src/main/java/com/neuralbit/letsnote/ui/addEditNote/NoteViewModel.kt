package com.neuralbit.letsnote.ui.addEditNote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.firebaseEntities.LabelFire
import com.neuralbit.letsnote.firebaseEntities.NoteFireIns
import com.neuralbit.letsnote.firebaseEntities.TagFire
import com.neuralbit.letsnote.firebaseEntities.TodoItem
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo
import java.util.*

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    val TAG = "NoteViewModel"
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()
    var oldTagList = ArrayList<String>()
    var newTags = HashSet<String>()
    var deletedTags = HashSet<String>()
    var noteChanged = MutableLiveData<Boolean>()
    var appPaused = false
    var adViewed = false
    var archived : MutableLiveData<Boolean> = MutableLiveData()
    var noteLocked = MutableLiveData<Boolean>()
    var deletedNote : MutableLiveData<Boolean> = MutableLiveData()
    var labelChanged : Boolean = false
    var labelColor : MutableLiveData<Int> = MutableLiveData()
    var labelTitle : MutableLiveData<String> = MutableLiveData()
    var pinned : MutableLiveData<Boolean> = MutableLiveData()
    var reminderSet : MutableLiveData<Boolean> = MutableLiveData()
    var reminderTime : Long = 0
    var searchQuery : MutableLiveData<String> = MutableLiveData<String>()
    var newTagTyped : MutableLiveData<Boolean> = MutableLiveData()
    var backPressed : MutableLiveData<Boolean> = MutableLiveData()
    var todoItems = LinkedList<TodoItem>()
    var allTodoItems = MutableLiveData<ArrayList<TodoItem>>()
    val updatedTodos = ArrayList<TodoItem>()
    var undoMode : MutableLiveData<Boolean> = MutableLiveData()
    var labelFireList = ArrayList<LabelFire>()

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

    fun addOrDeleteLabel(labelColor: Int,labelTitle : String? ,oldLabel : Int, noteUid: String, add: Boolean){
        labelFireRepo.addOrDeleteLabels(labelColor,oldLabel,noteUid,labelTitle,add)
    }

    fun addOrDeleteTags(newTagsAdded: HashSet<String>, deletedTags: HashSet<String>, noteUid: String) {
        tagFireRepo.addOrDeleteTags(newTagsAdded,deletedTags,noteUid)
    }

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteFireRepo.deleteNote(noteUid)
        tagFireRepo.deleteNoteFromTags(tagList,noteUid)
        labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
    }

}