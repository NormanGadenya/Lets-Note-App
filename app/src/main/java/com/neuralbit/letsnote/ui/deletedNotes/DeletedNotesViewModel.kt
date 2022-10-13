package com.neuralbit.letsnote.ui.deletedNotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
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

class DeletedNotesViewModel (application: Application): AndroidViewModel(application) {

    var searchQuery : MutableLiveData<String> = MutableLiveData()

    var clearTrash : MutableLiveData<Boolean> = MutableLiveData()
    var deleteFrag : MutableLiveData<Boolean> = MutableLiveData()
    var deletedNotes : HashSet<NoteFire> = HashSet()
    var itemRestoreClicked : MutableLiveData<Boolean> = MutableLiveData()
    var itemDeleteClicked : MutableLiveData<Boolean> = MutableLiveData()
    var useLocalStorage = false

    private val noteRoomDao = NoteDatabase.getDatabase(application).getNotesDao()
    private val noteRoomRepo = NoteRoomRepo(noteRoomDao)

    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()
    private val tagRoomRepo = TagRoomRepo(tagRoomDao)


    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)

    var undoDelete = true


    private val noteFireRepo = NoteFireRepo()
    private val tagFireRepo = TagFireRepo()
    private val labelFireRepo = LabelFireRepo()

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


}