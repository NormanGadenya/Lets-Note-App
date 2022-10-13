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
import com.neuralbit.letsnote.room.repos.LabelRoomRepo
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
    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)
    val TAG = "Label_Notes_View_Modal"

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteFireRepo.deleteNote(noteUid)
        tagFireRepo.deleteNoteFromTags(tagList,noteUid)
        labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
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