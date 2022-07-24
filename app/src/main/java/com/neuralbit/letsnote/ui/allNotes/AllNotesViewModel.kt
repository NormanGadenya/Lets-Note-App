package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.repos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AllNotesViewModel (application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    var otherFireNotesList = MutableLiveData<List<NoteFire>>()
    var pinnedFireNotesList = MutableLiveData<List<NoteFire>>()
    private val noteFireRepo: NoteFireRepo
    private val noteRepo : NoteRepo
    private val noteTagRepo : NoteTagRepo
    private val reminderRepo : ReminderRepo
    private val labelRepo : LabelRepo
    private val tagRepo : TagRepo
    private var allTags: LiveData<List<Tag>>


    var pinnedNotes: LiveData<List<Note>>
    var searchQuery : MutableLiveData<String> = MutableLiveData()

    init{

        val noteDao = NoteDatabase.getDatabase(application).getNotesDao()
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        val reminderDao = NoteDatabase.getDatabase(application).getReminderDao()
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()

        noteFireRepo = NoteFireRepo()
        noteRepo= NoteRepo(noteDao)
        noteTagRepo = NoteTagRepo(noteTagDao)
        reminderRepo = ReminderRepo(reminderDao)
        labelRepo = LabelRepo(labelDao)
        tagRepo = TagRepo(tagDao)
        allNotes = noteRepo.notesWithoutPinArc
        pinnedNotes = noteRepo.pinnedNotes
        allTags = tagRepo.allTags


    }

    fun deleteNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        noteRepo.delete(note)
    }



    fun filterOtherFireList () : LiveData<List<NoteFire>>{
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

    fun filterPinnedFireList () : LiveData<List<NoteFire>>{
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


    fun getAllFireNotes () :LiveData<List<NoteFire>> {
        return noteFireRepo.getAllNotes()
    }

    fun getTodoList(noteID: Long) : LiveData<List<TodoItem>>{
        return noteRepo.getTodoList(noteID)
    }

    fun updateTodoItem(todoItem: TodoItem)= viewModelScope.launch(Dispatchers.IO){
        noteRepo.updateTodo(todoItem)
    }
    fun deleteTodoItem(todoItem: TodoItem)= viewModelScope.launch(Dispatchers.IO){
        noteRepo.deleteTodo(todoItem)
    }


    private fun filterLiveList(list: List<Note>, text : String? ): List<Note>{
        var newList = ArrayList<Note>()

        return if(text!=null){
            var textLower= text.toLowerCase()
            for ( note in list){

                if(note.title?.toLowerCase()?.contains(textLower) == true || note.description?.toLowerCase()
                        ?.contains(textLower) == true
                ){
                    newList.add(note)
                }
            }

            newList
        }else{
            list
        }


    }

    private fun filterList(list : List<NoteFire>, text: String?) : List<NoteFire>{
        val newList = ArrayList<NoteFire>()

        return if (text != null) {
            val textLower= text.toLowerCase(Locale.ROOT)
            for ( note in list){

                if(note.title.toLowerCase(Locale.ROOT).contains(textLower) || note.description.toLowerCase(Locale.ROOT).contains(textLower)){
                    newList.add(note)
                }
            }
            newList
        }else{
            list
        }

    }

    fun getNoteLabel( noteID : Long) : LiveData <Label> {
        return labelRepo.getNoteLabel(noteID)
    }

    fun getReminder(noteID : Long): LiveData<Reminder>  {
        return reminderRepo.fetchReminder(noteID)
    }

    suspend fun getTagsWithNote(noteID: Long):List<TagsWithNote> {
        return noteTagRepo.getTagsWithNote(noteID)
    }

    fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef) = viewModelScope.launch(Dispatchers.IO){
        noteTagRepo.deleteNoteTagCrossRef(crossRef)
    }

    fun deleteNoteLabel(noteID: Long) = viewModelScope.launch(Dispatchers.IO){
        labelRepo.deleteNoteLabel(noteID)
    }
    fun deleteLabel(labelID: Int) = viewModelScope.launch(Dispatchers.IO){
        labelRepo.deleteLabel(labelID)
    }

    suspend fun insertDeleted(deletedNote: DeletedNote) {
        return noteRepo.insertDeletedNote(deletedNote)
    }

    fun deleteReminder(noteID: Long) = viewModelScope.launch(Dispatchers.IO){
        reminderRepo.delete(noteID)
    }

    fun removePin(pinnedNote: PinnedNote) = viewModelScope.launch(Dispatchers.IO){
        noteRepo.deletePinned(pinnedNote)
    }

    fun removeArchive(archivedNote: ArchivedNote) = viewModelScope.launch(Dispatchers.IO){
        noteRepo.deleteArchive(archivedNote)
    }

    fun archiveNote(archivedNote: ArchivedNote) = viewModelScope.launch(Dispatchers.IO){
        noteRepo.insertArchive(archivedNote)
    }
}