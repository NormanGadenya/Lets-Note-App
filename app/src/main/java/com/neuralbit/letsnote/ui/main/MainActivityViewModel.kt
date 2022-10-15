package com.neuralbit.letsnote.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.firebase.entities.TodoItem
import com.neuralbit.letsnote.firebase.repos.DeleteDataRepo
import com.neuralbit.letsnote.firebase.repos.LabelFireRepo
import com.neuralbit.letsnote.firebase.repos.NoteFireRepo
import com.neuralbit.letsnote.firebase.repos.TagFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.repos.LabelRoomRepo
import com.neuralbit.letsnote.room.repos.NoteRoomRepo
import com.neuralbit.letsnote.room.repos.NoteTagRoomRepo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivityViewModel(application : Application) : AndroidViewModel(application)  {
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    var useLocalStorage = false
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()
    private val deleteDataRepo = DeleteDataRepo()
    private val noteRoomDao = NoteDatabase.getDatabase(application).getNotesDao()
    private val noteRoomRepo = NoteRoomRepo(noteRoomDao)
    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)
    val refresh = MutableLiveData<Boolean>()

    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)


    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getAllFisreNotes () : LiveData<ArrayList<NoteFire>>{
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

    //TODO does not react after delete
    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        if (!useLocalStorage){
            noteFireRepo.deleteNote(noteUid)
            tagFireRepo.deleteNoteFromTags(tagList,noteUid)
            labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
        }else{
            GlobalScope.launch {
                for (tagTitle in tagList){
                    var tagStr = tagTitle
                    val split = tagStr.split("#")
                    if (split.size > 1){
                        tagStr = split[1]
                    }
                    val noteTagCrossRef = NoteTagCrossRef(noteUid,tagStr)
                    noteTagRoomRepo.deleteNoteTagCrossRef(noteTagCrossRef)

                }
                val oldTodoItems = noteRoomRepo.getTodoList(noteUid)
                for (oldTodoItem in oldTodoItems) {
                    noteRoomRepo.deleteTodo(oldTodoItem)
                }

                noteRoomRepo.delete(noteUid)
                labelRoomRepo.getNotesWithLabel(labelColor).forEach {
                    if (it.notes.isEmpty()){
                        labelRoomRepo.deleteLabel(labelColor)
                    }
                }
            }
        }
    }

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) {

        noteFireRepo.updateNote(noteUpdate,noteUid)
    }

    fun deleteUserDataContent(context : Context){
        deleteDataRepo.deleteUserData(context)
    }

}