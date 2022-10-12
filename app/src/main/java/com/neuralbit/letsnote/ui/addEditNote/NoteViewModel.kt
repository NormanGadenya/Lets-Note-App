package com.neuralbit.letsnote.ui.addEditNote

import android.app.Application
import android.util.Log
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
import com.neuralbit.letsnote.room.entities.Tag
import com.neuralbit.letsnote.room.relationships.LabelWIthNotes
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.repos.*
import com.neuralbit.letsnote.utilities.FirebaseKeyGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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
        if (!useLocalStorage){
            return tagFireRepo.getAllTags()
        }else{
            val getDataJob = GlobalScope.async {
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
                    tag.tagName = it.tagTitle
                    tag.noteUids = noteUids
                    tagFireList.add(tag)
//                    tagFireMutableData.value = tagFireList
                    Log.d(TAG, "allFireTags: $tagFireList")

                }
                return@async tagFireList
            }
            getDataJob.invokeOnCompletion {

            }


            // tell the job to invoke this code when it's done
            getDataJob.invokeOnCompletion { cause ->
                if (cause != null) {
                    // error!  Handle that here
                    Unit
                } else {
                    val myData = getDataJob.getCompleted()

                    // ITEM 1
                    // ***************************
                    // do something with your data
                    // ***************************

                    Unit  // this is just because the lambda here has to return Unit
                }
            }
            val tagFireMutableData = MutableLiveData<List<TagFire>>()
//                viewModelScope.launch {
//
//                }
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
                        tag.tagName = it.tagTitle
                        tag.noteUids = noteUids
                        tagFireList.add(tag)
                        tagFireMutableData.postValue(tagFireList)
                }
            }


            return tagFireMutableData
        }
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
        if (!useLocalStorage){
            tagFireRepo.addOrDeleteTags(newTagsAdded,deletedTags,noteUid)
        }else{
            viewModelScope.launch {
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
                }
            }
        }
    }

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteFireRepo.deleteNote(noteUid)
        tagFireRepo.deleteNoteFromTags(tagList,noteUid)
        labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
    }

    fun deleteRoomLabel (labelColor: Int) = viewModelScope.launch(Dispatchers.IO){
        labelRoomRepo.deleteLabel(labelColor)
    }

    fun deleteRoomTag (tagTitle: String) = viewModelScope.launch(Dispatchers.IO){
        val tag = Tag(tagTitle)
        tagRoomRepo.delete(tag)
    }

}