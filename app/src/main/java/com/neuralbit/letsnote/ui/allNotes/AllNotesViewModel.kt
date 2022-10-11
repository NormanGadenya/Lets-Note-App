package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseEntities.TodoItem
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.repos.*
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
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()
    private val tagRoomRepo = TagRoomRepo(tagRoomDao)

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


    suspend fun getAllFireNotes () : LiveData<ArrayList<NoteFire>>{
        if (!useLocalStorage){
            return noteFireRepo.getAllNotes()
        }else{
            return noteRoomRepo.allNotes.map {
                val mappedNotes = it.map { note ->
                    val noteFire = NoteFire()
                    noteFire.noteUid = note.noteUid
                    noteFire.description = note.description!!
                    noteFire.title = note.title!!
                    noteFire.timeStamp = note.timestamp
                    noteFire.label = note.labelColor
                    val reminder = reminderRoomRepo.fetchReminder(note.noteUid)
                    if (reminder.value != null){
                        noteFire.reminderDate = reminder.value!!.time
                    }
                    val archivedNote = noteRoomRepo.getArchivedNote(note.noteUid)
                    if (archivedNote.value != null){
                        noteFire.archived = true
                    }

                    val pinnedNote = noteRoomRepo.getPinnedNote(note.noteUid)
                    if (pinnedNote.value != null){
                        noteFire.pinned = true
                    }

                    val deleted = noteRoomRepo.getDeletedNote(note.noteUid)
                    if (deleted.value != null){
                        noteFire.deletedDate = deleted.value!!.timestamp
                    }

                    val protectedNote = noteRoomRepo.getProtectedNote(note.noteUid)
                    if (protectedNote.value != null){
                        noteFire.protected = true
                    }
//                    val tagRoomList = noteTagRoomRepo.getTagsWithNote(note.noteUid)
//                    for (t in tagRoomList) {
//                        val tagSList = ArrayList<String>()
//                        for (tag in t.tags) {
//                            tagSList.add(tag.tagTitle)
//                        }
//                        noteFire.tags = tagSList
//                    }
                    //TODO fix this
                    val todoItems = noteRoomRepo.getTodoList(note.noteUid).value
                    if (todoItems != null){
                        val items = todoItems.map { t -> TodoItem(item = t.itemDesc, checked = t.itemChecked) }
                        noteFire.todoItems = items
                    }
                    return@map noteFire
                }
                return@map ArrayList(mappedNotes)
            }
        }
    }

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) {
        noteFireRepo.updateNote(noteUpdate,noteUid)
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