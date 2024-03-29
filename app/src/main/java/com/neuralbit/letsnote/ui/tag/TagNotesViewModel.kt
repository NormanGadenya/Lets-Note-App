package com.neuralbit.letsnote.ui.tag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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

class TagNotesViewModel(
    application: Application) :AndroidViewModel(application){
    val searchQuery: MutableLiveData<String> = MutableLiveData()
    var noteUids = ArrayList<String>()
    var allTagNotes = ArrayList<NoteFire>()
    var selectedNotes = HashSet<NoteFire>()
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

    private val fUser = FirebaseAuth.getInstance().currentUser

    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)

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


}