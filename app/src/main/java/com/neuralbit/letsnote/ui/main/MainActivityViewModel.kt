package com.neuralbit.letsnote.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.firebase.repos.DeleteDataRepo
import com.neuralbit.letsnote.firebase.repos.LabelFireRepo
import com.neuralbit.letsnote.firebase.repos.NoteFireRepo
import com.neuralbit.letsnote.firebase.repos.TagFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.repos.LabelRoomRepo
import com.neuralbit.letsnote.room.repos.NoteRoomRepo
import com.neuralbit.letsnote.room.repos.NoteTagRoomRepo
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