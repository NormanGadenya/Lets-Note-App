package com.neuralbit.letsnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.entities.LabelFire
import com.neuralbit.letsnote.entities.NoteFireIns
import com.neuralbit.letsnote.entities.TagFire
import com.neuralbit.letsnote.entities.TodoItem
import com.neuralbit.letsnote.repos.LabelFireRepo
import com.neuralbit.letsnote.repos.NoteFireRepo
import com.neuralbit.letsnote.repos.TagFireRepo

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    val TAG = "NoteViewModel"
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()
    var oldTagList = ArrayList<String>()
    var newTags = HashSet<String>()
    var deletedTags = HashSet<String>()
    var noteChanged = MutableLiveData<Boolean>()
    var archived : MutableLiveData<Boolean> = MutableLiveData()
    var deletedNote : MutableLiveData<Boolean> = MutableLiveData()
    var labelChanged : Boolean = false
    var labelColor : Int = 0
    var pinned : MutableLiveData<Boolean> = MutableLiveData()
    var reminderSet : MutableLiveData<Boolean> = MutableLiveData()
    var labelSet : MutableLiveData<Boolean> = MutableLiveData()
    var reminderTime : Long = 0
    var searchQuery : MutableLiveData<String> = MutableLiveData<String>()
    var newTagTyped : MutableLiveData<Boolean> = MutableLiveData()
    var backPressed : MutableLiveData<Boolean> = MutableLiveData()
    var todoItems = ArrayList<TodoItem>()
    var allTodoItems = MutableLiveData<ArrayList<TodoItem>>()
    val updatedTodos = ArrayList<TodoItem>()
    var undoMode : MutableLiveData<Boolean> = MutableLiveData()

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) {
        noteFireRepo.updateNote(noteUpdate,noteUid)
    }

    fun addFireNote(note: NoteFireIns) : String ?{
        return noteFireRepo.addNote(note)
    }

    fun allTodoItems(noteUid: String) : LiveData<List<TodoItem>>{
        val note = noteFireRepo.getNote(noteUid).value
        val todoItems = MutableLiveData<List<TodoItem>>()
        if (note != null){
            todoItems.value = note.todoItems
        }
        return todoItems
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