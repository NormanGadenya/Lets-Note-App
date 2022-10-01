package com.neuralbit.letsnote.ui.tag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo

class TagNotesViewModel(
    application: Application) :AndroidViewModel(application){
    val searchQuery: MutableLiveData<String> = MutableLiveData()
    var noteUids = ArrayList<String>()
    var allTagNotes = ArrayList<NoteFire>()
    var selectedNotes = HashSet<NoteFire>()
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteFireRepo.deleteNote(noteUid)
        tagFireRepo.deleteNoteFromTags(tagList,noteUid)
        labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
    }


}