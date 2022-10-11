package com.neuralbit.letsnote.ui.addEditNote

import android.app.Application
import androidx.lifecycle.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.neuralbit.letsnote.firebaseEntities.*
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.entities.ArchivedNote
import com.neuralbit.letsnote.room.entities.Label
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.relationships.LabelWIthNotes
import com.neuralbit.letsnote.room.repos.*
import com.neuralbit.letsnote.utilities.FirebaseKeyGenerator
import kotlinx.coroutines.Dispatchers
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
    val labelRoomRepo = LabelRoomRepo(labelRoomDao)

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()
    private val tagRoomRepo = TagRoomRepo(tagRoomDao)

    private val reminderRoomDao = NoteDatabase.getDatabase(application).getReminderDao()
    private val reminderRoomRepo = ReminderRoomRepo(reminderRoomDao)

    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) =viewModelScope.launch(Dispatchers.IO) {
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
        val archivedNote = ArchivedNote(noteUid)
        if (noteFireUpdate.archived){
            noteRoomRepo.insertArchive(archivedNote)
        }else{
            noteRoomRepo.deleteArchive(archivedNote)
        }
        noteRoomRepo.update(note)
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
            viewModelScope.launch {
                noteRoomRepo.insert(roomNote)
            }
            return noteUid
        }
    }

    fun allFireTags() : LiveData<List<TagFire>>{
        return tagFireRepo.getAllTags()
    }

    fun allRoomNotesWithLabel(labelColor: Int) : LiveData<List<LabelWIthNotes>>{
        return labelRoomRepo.getNotesWithLabel(labelColor)
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
            viewModelScope.launch {
                val label = Label(labelColor, labelTitle)
                labelRoomRepo.insert(label)
            }
        }
    }

    fun addOrDeleteTags(newTagsAdded: HashSet<String>, deletedTags: HashSet<String>, noteUid: String) {
        tagFireRepo.addOrDeleteTags(newTagsAdded,deletedTags,noteUid)
    }

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteFireRepo.deleteNote(noteUid)
        tagFireRepo.deleteNoteFromTags(tagList,noteUid)
        labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
    }

    fun deleteRoomLabel (labelColor: Int) = viewModelScope.launch(Dispatchers.IO){
        labelRoomRepo.deleteLabel(labelColor)
    }

}