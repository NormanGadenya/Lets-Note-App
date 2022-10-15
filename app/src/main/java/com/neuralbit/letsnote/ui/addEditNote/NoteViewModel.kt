package com.neuralbit.letsnote.ui.addEditNote

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.firebase.entities.*
import com.neuralbit.letsnote.firebase.repos.LabelFireRepo
import com.neuralbit.letsnote.firebase.repos.NoteFireRepo
import com.neuralbit.letsnote.firebase.repos.TagFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.entities.Label
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.Tag
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.repos.LabelRoomRepo
import com.neuralbit.letsnote.room.repos.NoteRoomRepo
import com.neuralbit.letsnote.room.repos.NoteTagRoomRepo
import com.neuralbit.letsnote.room.repos.TagRoomRepo
import com.neuralbit.letsnote.utilities.FirebaseKeyGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    var useLocalStorage = false

    private val noteRoomDao = NoteDatabase.getDatabase(application).getNotesDao()
    private val noteRoomRepo = NoteRoomRepo(noteRoomDao)

    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()
    private val tagRoomRepo = TagRoomRepo(tagRoomDao)


    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) =viewModelScope.launch(Dispatchers.IO) {
        if (!useLocalStorage){
            noteFireRepo.updateNote(noteUpdate,noteUid)
        }else{
            Log.d(TAG, "updateFireNote: $noteUpdate")

            val mapper = ObjectMapper() // jackson's objectmapper
            val noteFireUpdate: NoteFire = mapper.convertValue(noteUpdate, NoteFire::class.java)

            val note = Note(
                noteFireUpdate.title,
                noteFireUpdate.description,
                noteFireUpdate.timeStamp,
                noteFireUpdate.label,
                noteFireUpdate.archived,
                noteFireUpdate.pinned,
                noteFireUpdate.protected,
                noteFireUpdate.deletedDate,
                noteFireUpdate.reminderDate,
                noteUid)
            GlobalScope.launch {
                val oldTodoItems = noteRoomRepo.getTodoList(noteUid)
                for (oldTodoItem in oldTodoItems) {
                    noteRoomRepo.deleteTodo(oldTodoItem)
                }
                val newTodoItems = noteFireUpdate.todoItems
                for (todoItem in newTodoItems){
                    val todo = com.neuralbit.letsnote.room.entities.TodoItem(noteUid,todoItem.item,todoItem.checked)
                    noteRoomRepo.insertTodo(todo)
                }

            }


            noteRoomRepo.update(note)
        }
    }

    fun addFireNote(note: NoteFireIns) : String ? {
        if (!useLocalStorage){
            return noteFireRepo.addNote(note)

        }else{
            val noteUid = FirebaseKeyGenerator.generateKey()

            val roomNote = Note(
                note.title,
                note.description,
                note.timeStamp,
                note.label,
                note.archived,
                note.pinned,
                note.protected,
                note.deletedDate,
                note.reminderDate,
                noteUid)
            val todoItems = note.todoItems
            viewModelScope.launch {

                for (todoItem in todoItems){
                    val todo = com.neuralbit.letsnote.room.entities.TodoItem(noteUid,todoItem.item,todoItem.checked)
                    noteRoomRepo.insertTodo(todo)
                }
                noteRoomRepo.insert(roomNote)

            }
            return noteUid
        }
    }

     fun allFireTags() : LiveData<List<TagFire>>{
        if (!useLocalStorage){
            return tagFireRepo.getAllTags()
        }else{
            val tagFireMutableData = MutableLiveData<List<TagFire>>()
            GlobalScope.launch {
                val tagFireList = ArrayList<TagFire>()
                tagRoomRepo.getAllTags().forEach {
                    val tagList = noteTagRoomRepo.getNotesWithTag(tagTitle = it.tagTitle)
                    val noteUids = ArrayList<String>()
                    for (notesWithTag in tagList) {
                        for ( n in notesWithTag.notes){
                            noteUids.add(n.noteUid)
                        }
                    }
                    val tag = TagFire()
                    tag.tagName = "#${it.tagTitle}"
                    tag.noteUids = noteUids
                    tagFireList.add(tag)
                    tagFireMutableData.postValue(tagFireList)
                }
            }
            return tagFireMutableData
        }
    }

    fun allFireLabels() : LiveData<List<LabelFire>>{
        if (!useLocalStorage){
            return labelFireRepo.getAllLabels()
        }else{
            return labelRoomRepo.getAllLabels().map {
                val mappedLabels = it.map { l ->
                    val noteUids = l.notes.map { note -> note.noteUid }
                    val labelFire = LabelFire()
                    labelFire.labelColor = l.label.labelColor
                    labelFire.noteUids = ArrayList(noteUids)
                    if (l.label.labelTitle != null){
                        labelFire.labelTitle = l.label.labelTitle
                    }
                    return@map labelFire
                }
                return@map ArrayList(mappedLabels)
            }
        }
    }

    fun addOrDeleteLabel(labelColor: Int,labelTitle : String? ,oldLabel : Int, noteUid: String, add: Boolean){
        if(!useLocalStorage){
            labelFireRepo.addOrDeleteLabels(labelColor,oldLabel,noteUid,labelTitle,add)
        }else{
            GlobalScope.launch {
                if (oldLabel > 0 && oldLabel != labelColor){
                    labelRoomRepo.getNotesWithLabel(oldLabel).forEach {
                        if (it.notes.isEmpty()){
                            labelRoomRepo.deleteLabel(oldLabel)
                        }
                    }
                }
                if (add){
                    val label = Label(labelColor, labelTitle)
                    labelRoomRepo.insert(label)
                }
            }
        }
    }


    fun addOrDeleteTags(newTagsAdded: HashSet<String>, deletedTags: HashSet<String>, noteUid: String) {
        if (!useLocalStorage){
            tagFireRepo.addOrDeleteTags(newTagsAdded,deletedTags,noteUid)
        }else{
            GlobalScope.launch {
                for (tagTitle in newTagsAdded) {
                    var tagStr = tagTitle
                    val split = tagStr.split("#")
                    if (split.size > 1){
                        tagStr = split[1]
                    }
                    val tag = Tag(tagStr)

                    tagRoomRepo.insert(tag)
                    val noteTagCrossRef = NoteTagCrossRef(noteUid,tagStr)
                    noteTagRoomRepo.insertNoteTagCrossRef(noteTagCrossRef)

                }

                for (tagTitle in deletedTags){
                    var tagStr = tagTitle
                    val split = tagStr.split("#")
                    if (split.size > 1){
                        tagStr = split[1]
                    }
                    val noteTagCrossRef = NoteTagCrossRef(noteUid,tagStr)
                    noteTagRoomRepo.deleteNoteTagCrossRef(noteTagCrossRef)

                    noteTagRoomRepo.getNotesWithTag(tagStr).forEach {
                        if (it.notes.isEmpty()){
                            tagRoomRepo.delete(Tag(tagStr))
                        }
                    }
                }

            }
        }
    }

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        if (!useLocalStorage){
            noteFireRepo.deleteNote(noteUid)
            tagFireRepo.deleteNoteFromTags(tagList,noteUid)
            labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
        }else{
            GlobalScope.launch {
                for (tagTitle in tagList){
                    var tagStr = tagTitle
                    val split = tagStr.split("#")
                    if (split.size > 1){
                        tagStr = split[1]
                    }
                    val noteTagCrossRef = NoteTagCrossRef(noteUid,tagStr)
                    noteTagRoomRepo.deleteNoteTagCrossRef(noteTagCrossRef)

                }
                val oldTodoItems = noteRoomRepo.getTodoList(noteUid)
                for (oldTodoItem in oldTodoItems) {
                    noteRoomRepo.deleteTodo(oldTodoItem)
                }

                noteRoomRepo.delete(noteUid)
                labelRoomRepo.getNotesWithLabel(labelColor).forEach {
                    if (it.notes.isEmpty()){
                        labelRoomRepo.deleteLabel(labelColor)
                    }
                }
            }
        }
    }

}