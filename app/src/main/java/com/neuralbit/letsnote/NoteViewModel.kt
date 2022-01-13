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
import kotlin.collections.ArrayList

class NoteViewModel(application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    var deletedNotes: LiveData<List<Note>>
    var allTags: LiveData<List<Tag>>
    val TAG = "NoteViewModel"
    private val noteRepo : NoteRepo
    private val tagRepo : TagRepo
    private val noteTagRepo : NoteTagRepo
    private val reminderRepo : ReminderRepo
    private val labelRepo : LabelRepo
    var noteChanged = MutableLiveData<Boolean>()
    var deleted = MutableLiveData<Boolean>()
    var archived : MutableLiveData<Boolean>
    var deletedNote : MutableLiveData<Boolean>
    var pinned : MutableLiveData<Boolean>
    var reminderSet : MutableLiveData<Boolean>
    var labelSet : MutableLiveData<Boolean>
    var searchQurery : MutableLiveData<String>
    var archivedNote : LiveData<List<Note>>
    var pinnedNotes : LiveData<List<Note>>
    var noteDescString : MutableLiveData<String>
    var newTagTyped : MutableLiveData<Boolean>
    var backPressed : MutableLiveData<Boolean>
    var tagList : ArrayList<Tag>

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
        deletedNotes = noteRepo.deletedNotes
        allTags = tagRepo.allTags
        searchQurery = MutableLiveData<String>()
        noteDescString = MutableLiveData()
        newTagTyped = MutableLiveData()
        backPressed = MutableLiveData()
        tagList = ArrayList<Tag>()
        pinned = MutableLiveData()
        archived = MutableLiveData()
        deletedNote = MutableLiveData()
        reminderSet = MutableLiveData()
        labelSet = MutableLiveData()
        deleted = MutableLiveData()

    }





    fun addTagToList(tag : Tag) {

        if (!tagList.contains(tag)){
            tagList.add(tag)

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
    suspend fun insertDeleted(deletedNote: DeletedNote) {
        return noteRepo.insertDeletedNote(deletedNote)
    }
    suspend fun restoreDeleted(deletedNote: DeletedNote) {
        return noteRepo.restoreDeletedNote(deletedNote)
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



    suspend fun getTagsWithNote(noteID: Long):List<TagsWithNote> {
        return noteTagRepo.getTagsWithNote(noteID)
    }

    fun getArchivedNote(noteID: Long):LiveData<ArchivedNote> {
        return noteRepo.getArchivedNote(noteID)
    }
    fun getDeletedNote(noteID: Long):LiveData<DeletedNote> {
        return noteRepo.getDeletedNote(noteID)
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


    fun getNoteLabel( noteID : Long) : LiveData <Label> {
        return labelRepo.getNoteLabel(noteID)
    }


}