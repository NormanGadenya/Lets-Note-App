package com.neuralbit.letsnote.ui.deletedNotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo

class DeletedNotesViewModel (application: Application): AndroidViewModel(application) {

    var searchQuery : MutableLiveData<String> = MutableLiveData()

    var clearTrash : MutableLiveData<Boolean> = MutableLiveData()
    var deleteFrag : MutableLiveData<Boolean> = MutableLiveData()
    var deletedNotes : HashSet<NoteFire> = HashSet()
    var itemRestoreClicked : MutableLiveData<Boolean> = MutableLiveData()
    var itemDeleteClicked : MutableLiveData<Boolean> = MutableLiveData()
    var undoDelete = true


    private val noteRepo = NoteFireRepo()
    private val tagRepo = TagFireRepo()
    private val labelRepo = LabelFireRepo()

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteRepo.deleteNote(noteUid)
        tagRepo.deleteNoteFromTags(tagList,noteUid)
        labelRepo.deleteNoteFromLabel(labelColor,noteUid)
    }



}