package com.neuralbit.letsnote.ui.label

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo

class LabelNotesViewModel(
    application: Application) :AndroidViewModel(application){
    var labelTitle: String? = null
    var labelColor: Int = 0
    val searchQuery: MutableLiveData<String> = MutableLiveData()
    var selectedNotes = HashSet<NoteFire>()
    var labelNotes = ArrayList<NoteFire>()
    var noteUids = ArrayList<String>()
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteFireRepo.deleteNote(noteUid)
        tagFireRepo.deleteNoteFromTags(tagList,noteUid)
        labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
    }

    fun updateLabel (labelUpdate : Map<String,Any>, labelColor : Int){
        labelFireRepo.updateNote(labelUpdate, labelColor)
    }
}