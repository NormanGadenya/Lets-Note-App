package com.neuralbit.letsnote.ui.label

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.firebase.repos.LabelFireRepo
import com.neuralbit.letsnote.firebase.repos.NoteFireRepo
import com.neuralbit.letsnote.firebase.repos.TagFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.repos.LabelRoomRepo
import com.neuralbit.letsnote.room.repos.NoteRoomRepo
import com.neuralbit.letsnote.room.repos.NoteTagRoomRepo
import com.neuralbit.letsnote.room.repos.TagRoomRepo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LabelNotesViewModel(
    application: Application) :AndroidViewModel(application){
    var labelTitle: String? = null
    var labelColor: Int = 0
    val searchQuery: MutableLiveData<String> = MutableLiveData()
    var selectedNotes = HashSet<NoteFire>()
    var labelNotes = ArrayList<NoteFire>()
    var noteUids = ArrayList<String>()
//    var useLocalStorage = false
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()

    private val noteRoomDao = NoteDatabase.getDatabase(application).getNotesDao()
    private val noteRoomRepo = NoteRoomRepo(noteRoomDao)

    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()
    private val tagRoomRepo = TagRoomRepo(tagRoomDao)


    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)
    private val fUser = FirebaseAuth.getInstance().currentUser

    val TAG = "Label_Notes_View_Modal"

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        if (fUser != null){
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

    fun updateLabel ( labelTitle : String , newLabelColor: Int){
        if (fUser != null){
//            labelFireRepo.updateNote(labelUpdate, labelColor)
            labelFireRepo.updateLabelColorOrTitle(labelTitle,labelColor.toString(),newLabelColor.toString() )
        }else{
            val newLabelTitle = labelTitle
            val label = com.neuralbit.letsnote.room.entities.Label(labelColor, newLabelTitle)
            viewModelScope.launch {
                labelRoomRepo.insert(label)
            }

        }
    }

}