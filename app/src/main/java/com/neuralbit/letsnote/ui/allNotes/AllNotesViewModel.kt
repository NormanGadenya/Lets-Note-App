package com.neuralbit.letsnote.ui.allNotes

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.NoteDatabase
import com.neuralbit.letsnote.daos.LabelDao
import com.neuralbit.letsnote.entities.Label
import com.neuralbit.letsnote.entities.Reminder
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

    var pinnedNotes: LiveData<List<Note>>
    var searchQuery : MutableLiveData<String> = MutableLiveData()

    init{

        val noteDao = NoteDatabase.getDatabase(application).getNotesDao()
        val noteTagDao = NoteDatabase.getDatabase(application).getNoteTagDao()
        val reminderDao = NoteDatabase.getDatabase(application).getReminderDao()
        val labelDao = NoteDatabase.getDatabase(application).getLabelDao()

        noteRepo= NoteRepo(noteDao)
        noteTagRepo = NoteTagRepo(noteTagDao)
        reminderRepo = ReminderRepo(reminderDao)
        labelRepo = LabelRepo(labelDao)
        allNotes = noteRepo.allNotes
        pinnedNotes = noteRepo.pinnedNotes

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
}