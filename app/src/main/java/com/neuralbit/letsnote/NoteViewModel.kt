package com.neuralbit.letsnote

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.relationships.LabelWIthNotes
import com.neuralbit.letsnote.relationships.NotesWithTag
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.repos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.sql.Date
import java.sql.Time
import kotlin.collections.ArrayList

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    var allTags: LiveData<List<Tag>>
    val TAG = "NoteViewModel"
    private val noteRepo : NoteRepo
    private val tagRepo : TagRepo
    private val noteTagRepo : NoteTagRepo
    private val reminderRepo : ReminderRepo
    private val labelRepo : LabelRepo
    var texChange : Boolean
    var texChanged = MutableLiveData<Boolean>()
    var deleted = MutableLiveData<Boolean>()
    var archived : MutableLiveData<Boolean>
    var pinned : MutableLiveData<Boolean>
    var reminderSet : MutableLiveData<Boolean>
    var labelSet : MutableLiveData<Boolean>
    lateinit var list : List<Note>
    var searchQurery : MutableLiveData<String>
    var archivedNote : LiveData<List<Note>>
    var pinnedNotes : LiveData<List<Note>>
    var wordStart : MutableLiveData<Int>
    var wordEnd : MutableLiveData<Int>
    var noteDescString : MutableLiveData<String>
    var newTagTyped : MutableLiveData<Boolean>
    var backPressed : MutableLiveData<Boolean>
    var enterPressed : MutableLiveData<Boolean>
    var tagList : ArrayList<Tag>
    val reminderDate : MutableLiveData<Long>
    val reminderTime : MutableLiveData<Long>
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
        allNotes = noteRepo.allNotes
        archivedNote = noteRepo.archivedNotes
        pinnedNotes = noteRepo.pinnedNotes
        allTags = tagRepo.allTags
        texChange = false
        searchQurery = MutableLiveData<String>()
        wordStart = MutableLiveData()
        wordEnd = MutableLiveData()
        noteDescString = MutableLiveData()
        newTagTyped = MutableLiveData()
        backPressed = MutableLiveData()
        enterPressed = MutableLiveData()
        tagList = ArrayList<Tag>()
        pinned = MutableLiveData()
        archived = MutableLiveData()
        reminderSet = MutableLiveData()
        labelSet = MutableLiveData()

        deleted = MutableLiveData()
        reminderDate = MutableLiveData()
        reminderTime = MutableLiveData()
    }

    fun getTagString(text: String){
        noteDescString.value = text.substring(wordStart.value!!, wordEnd.value!!)

    }



    fun addTagToList(tag : Tag) {

        if (!tagList.contains(tag)){
            tagList.add(tag)

        }
    }


    fun noteChanged (b : Boolean){
        texChange = b
        texChanged.value= texChange
    }

    fun filterList( ) : LiveData<List<Tag>>  {
        val textLower = noteDescString.value
        Log.d(TAG, "filterList: ${noteDescString.value}")
        return Transformations.map(allTags,){
            filterLiveList(it,textLower)
        }
    }
    private fun filterLiveList(list: List<Tag>, text : String? ): List<Tag>{
        val newList = ArrayList<Tag>()

        return if(text!=null){
            val textLower= text.toLowerCase()
            for ( tag in list){

                if(tag.tagTitle.toLowerCase().contains(textLower)  ){
                    newList.add(tag)

                }
            }

            newList
        }else{
            list
        }


    }

    fun  getNote(noteID: Long) : LiveData<Note>{
        return noteRepo.getNote(noteID)
    }

    fun deleteNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        noteRepo.delete(note)
    }
    fun updateNote(note: Note)= viewModelScope.launch(Dispatchers.IO){
        noteRepo.update(note)
    }
    suspend fun addNote(note: Note) : Long{
        return noteRepo.insert(note)
    }
    fun archiveNote(archivedNote: ArchivedNote) = viewModelScope.launch(Dispatchers.IO){
        noteRepo.insertArchive(archivedNote)
    }

    fun removeArchive(archivedNote: ArchivedNote) = viewModelScope.launch(Dispatchers.IO){
        noteRepo.deleteArchive(archivedNote)
    }

    fun pinNote(pinnedNote: PinnedNote) = viewModelScope.launch(Dispatchers.IO){
        noteRepo.insertPinned(pinnedNote)
    }

    fun removePin(pinnedNote: PinnedNote) = viewModelScope.launch(Dispatchers.IO){
        noteRepo.deletePinned(pinnedNote)
    }

    fun addTag(tag : Tag) = viewModelScope.launch(Dispatchers.IO){
        tagRepo.insert(tag)
    }

    fun deleteTag(tag : Tag) = viewModelScope.launch(Dispatchers.IO){
        tagRepo.delete(tag)
    }

    fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef) = viewModelScope.launch(Dispatchers.IO){
        noteTagRepo.insertNoteTagCrossRef(crossRef)
    }
    fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef) = viewModelScope.launch(Dispatchers.IO){
        noteTagRepo.deleteNoteTagCrossRef(crossRef)
    }

    suspend fun  getNotesWithTag(tagTitle : String) : List<NotesWithTag> {
       return noteTagRepo.getNotesWithTag(tagTitle)
    }

    suspend fun getTagsWithNote(noteID: Long):List<TagsWithNote> {
        return noteTagRepo.getTagsWithNote(noteID)
    }

    fun getArchivedNote(noteID: Long):LiveData<ArchivedNote> {
        return noteRepo.getArchivedNote(noteID)
    }

    fun getPinnedNote(noteID: Long):LiveData<PinnedNote> {
        return noteRepo.getPinnedNote(noteID)
    }

    fun insertReminder(reminder: Reminder) = viewModelScope.launch(Dispatchers.IO){
        reminderRepo.insert(reminder)
    }

    fun deleteReminder(noteID: Long) = viewModelScope.launch(Dispatchers.IO){
        reminderRepo.delete(noteID)
    }
    fun getReminder(noteID : Long): LiveData<Reminder>  {
        return reminderRepo.fetchReminder(noteID)
    }

    fun insertLabel(label: Label) = viewModelScope.launch(Dispatchers.IO){
        labelRepo.insert(label)
    }

    fun deleteNoteLabel(noteID: Long) = viewModelScope.launch(Dispatchers.IO){
        labelRepo.deleteNoteLabel(noteID)
    }
    fun deleteLabel(labelID: Int) = viewModelScope.launch(Dispatchers.IO){
        labelRepo.deleteLabel(labelID)
    }

    fun getNotesWithLabel ( labelID : Int) : LiveData<List<LabelWIthNotes>>{
        return labelRepo.getNotesWithLabel(labelID)
    }

    fun getNoteLabel( noteID : Long) : LiveData <Label> {
        return labelRepo.getNoteLabel(noteID)
    }


}