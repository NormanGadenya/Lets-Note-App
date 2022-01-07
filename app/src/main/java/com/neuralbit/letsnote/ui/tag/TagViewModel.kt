package com.neuralbit.letsnote.ui.tag

import android.app.Application
import androidx.lifecycle.*
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.relationships.NotesWithTag
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.repos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagViewModel (application: Application): AndroidViewModel(application) {
    var allTags: LiveData<List<Tag>>
    private val noteTagRepo : NoteTagRepo
    private val tagRepo : TagRepo
    private val noteRepo : NoteRepo
    private val reminderRepo : ReminderRepo
    private val labelRepo : LabelRepo
    var searchQuery : MutableLiveData <String>
    init {
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        val noteDao = NoteDatabase.getDatabase(application).getNotesDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()
        val reminderDao = NoteDatabase.getDatabase(application).getReminderDao()
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()
        noteTagRepo = NoteTagRepo(noteTagDao)
        tagRepo = TagRepo(tagDao)
        noteRepo = NoteRepo(noteDao)
        reminderRepo = ReminderRepo(reminderDao)
        labelRepo = LabelRepo(labelDao)
        allTags = tagRepo.allTags
        searchQuery = MutableLiveData()
    }
    suspend fun getNotesWithTag(tagTitle : String) : List<NotesWithTag> {
        return noteTagRepo.getNotesWithTag(tagTitle)
    }

    suspend fun getTagsWithNote(noteID: Long):List<TagsWithNote> {
        return noteTagRepo.getTagsWithNote(noteID)
    }

    fun deleteNoteTagCrossRef(crossRef: NoteTagCrossRef) = viewModelScope.launch(Dispatchers.IO){
        noteTagRepo.deleteNoteTagCrossRef(crossRef)
    }

    fun deleteLabel(noteID: Long) = viewModelScope.launch(Dispatchers.IO){
        labelRepo.delete(noteID)
    }
    fun deleteTag(tag: Tag) = viewModelScope.launch(Dispatchers.IO){
        tagRepo.delete(tag)
    }

    fun deleteNote(note : Note) = viewModelScope.launch(Dispatchers.IO){
            noteRepo.delete(note)
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
}