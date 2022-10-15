package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.firebase.entities.TodoItem
import com.neuralbit.letsnote.firebase.repos.NoteFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.repos.NoteRoomRepo
import com.neuralbit.letsnote.room.repos.NoteTagRoomRepo
import com.neuralbit.letsnote.room.repos.ReminderRoomRepo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    var useLocalStorage = false
    var signedIn = false
    var archiveFrag = false

    private val noteRoomDao = NoteDatabase.getDatabase(application).getNotesDao()
    private val noteRoomRepo = NoteRoomRepo(noteRoomDao)

    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()

    private val reminderRoomDao = NoteDatabase.getDatabase(application).getReminderDao()
    private val reminderRoomRepo = ReminderRoomRepo(reminderRoomDao)

    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)


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


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getAllFireNotes () : LiveData<ArrayList<NoteFire>>{
        if (useLocalStorage){
            val mutableNoteData = MutableLiveData<ArrayList<NoteFire>>()
            GlobalScope.launch {
                val noteList = ArrayList<NoteFire>()
                for (note in noteRoomRepo.getAllNotes()) {
                    val noteFire = NoteFire()
                    noteFire.noteUid = note.noteUid
                    noteFire.description = note.description!!
                    noteFire.title = note.title!!
                    noteFire.timeStamp = note.timestamp
                    noteFire.label = note.labelColor
                    noteFire.protected = note.locked
                    noteFire.archived = note.archived
                    noteFire.pinned = note.pinned
                    noteFire.deletedDate = note.deletedDate
                    noteFire.reminderDate = note.reminderDate
                    val tagsList = ArrayList<String>()
                    for (tagsWithNote in noteTagRoomRepo.getTagsWithNote(note.noteUid)) {
                        for (t in tagsWithNote.tags){
                            tagsList.add("#${t.tagTitle}")
                        }
                    }
                    noteFire.tags = tagsList
                    val todoItems = noteRoomRepo.getTodoList(note.noteUid)
                    val items = todoItems.map { t -> TodoItem(item = t.itemDesc, checked = t.itemChecked) }
                    noteFire.todoItems = items
                    noteList.add(noteFire)

                }
                mutableNoteData.postValue(noteList)
            }
            return mutableNoteData
        }else{
            return noteFireRepo.getAllNotes()
        }
    }


    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) =viewModelScope.launch(Dispatchers.IO) {
        if (!useLocalStorage){
            noteFireRepo.updateNote(noteUpdate,noteUid)
        }else{
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