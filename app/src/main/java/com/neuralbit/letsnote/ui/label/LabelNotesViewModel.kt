package com.neuralbit.letsnote.ui.label

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo
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
    var useLocalStorage = false
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

    val TAG = "Label_Notes_View_Modal"

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

    fun updateLabel (labelUpdate : Map<String,String>, labelColor : Int){
        if (!useLocalStorage){

            labelFireRepo.updateNote(labelUpdate, labelColor)
        }else{
            val newLabelTitle = labelUpdate["labelTitle"]
            val label = com.neuralbit.letsnote.room.entities.Label(labelColor, newLabelTitle)
            Log.d(TAG, "updateLabel: $label")
            viewModelScope.launch {
                labelRoomRepo.insert(label)
            }

        }
    }
}