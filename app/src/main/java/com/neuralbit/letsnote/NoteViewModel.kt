package com.neuralbit.letsnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.repos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    var deletedNotes: LiveData<List<Note>>
    val TAG = "NoteViewModel"
    private val noteRepo : NoteRepo
    private val tagRepo : TagRepo
    private val noteTagRepo : NoteTagRepo
    private val reminderRepo : ReminderRepo
    private val labelRepo : LabelRepo
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
    private var archivedNote : LiveData<List<Note>>
    private var pinnedNotes : LiveData<List<Note>>
    var noteDescString : MutableLiveData<String>
    var newTagTyped : MutableLiveData<Boolean>
    var backPressed : MutableLiveData<Boolean>
    var undoMode : MutableLiveData<Boolean>

    init{

        val dao = NoteDatabase.getDatabase(application).getNotesDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        val reminderDao = NoteDatabase.getDatabase(application).getReminderDao()
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()
        noteRepo= NoteRepo(dao)
        tagRepo = TagRepo(tagDao)
        noteTagRepo = NoteTagRepo(noteTagDao)
        reminderRepo = ReminderRepo(reminderDao)
        labelRepo = LabelRepo(labelDao)
        noteFireRepo = NoteFireRepo()
        tagFireRepo = TagFireRepo()
        labelFireRepo = LabelFireRepo()
        allNotes = noteRepo.allNotes
        archivedNote = noteRepo.archivedNotes
        pinnedNotes = noteRepo.pinnedNotes
        deletedNotes = noteRepo.deletedNotes
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


    fun  getNote(noteID: Long) : LiveData<Note>{
        return noteRepo.getNote(noteID)
    }

    fun getTodoList(noteID: Long) : LiveData<List<TodoItem>>{
        return noteRepo.getTodoList(noteID)
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

    fun updateTodoItem(todoItem: TodoItem)= viewModelScope.launch(Dispatchers.IO){
        noteRepo.updateTodo(todoItem)
    }
    fun deleteTodoItem(todoItem: TodoItem)= viewModelScope.launch(Dispatchers.IO){
        noteRepo.deleteTodo(todoItem)
    }
    suspend fun addTodoItem(todoItem: TodoItem){
        noteRepo.insertTodo(todoItem)
    }
    suspend fun insertDeleted(deletedNote: DeletedNote) {
        return noteRepo.insertDeletedNote(deletedNote)
    }
    suspend fun restoreDeleted(deletedNote: DeletedNote) {
        return noteRepo.restoreDeletedNote(deletedNote)
    }


    fun addOrDeleteTags(newTagsAdded: HashSet<String>, deletedTags: HashSet<String>, noteUid: String) {
        tagFireRepo.addOrDeleteTags(newTagsAdded,deletedTags,noteUid)
    }


}