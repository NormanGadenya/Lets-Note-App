package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.daos.LabelDao
import com.neuralbit.letsnote.entities.*
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.repos.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.ArrayList

class AllNotesViewModel (application : Application) : AndroidViewModel(application) {
    var allNotes: LiveData<List<Note>>
    private val noteRepo : NoteRepo
    private val noteTagRepo : NoteTagRepo
    private val reminderRepo : ReminderRepo
    private val labelRepo : LabelRepo
    private val tagRepo : TagRepo
    var allTags: LiveData<List<Tag>>


    var pinnedNotes: LiveData<List<Note>>
    var searchQuery : MutableLiveData<String> = MutableLiveData()

    init{

        val noteDao = NoteDatabase.getDatabase(application).getNotesDao()
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        val reminderDao = NoteDatabase.getDatabase(application).getReminderDao()
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()
        val tagDao = NoteDatabase.getDatabase(application).getTagDao()

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

    fun filterList( ) : LiveData<List<Note>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(allNotes,){
                filterLiveList(it,textLower)
            }
        }else{
            allNotes
        }

    }

    fun filterPinnedList( ) : LiveData<List<Note>>{
        val textLower = searchQuery.value
        Log.d("LOG", "filterList:${searchQuery.value} ")
        return if (searchQuery.value!=null){
            Transformations.map(pinnedNotes,){
                filterLiveList(it,textLower)
            }
        }else{
            pinnedNotes
        }

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