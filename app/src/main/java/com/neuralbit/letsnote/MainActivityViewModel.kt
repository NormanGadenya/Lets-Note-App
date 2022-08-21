package com.neuralbit.letsnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.entities.LabelFire
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.entities.TagFire
import com.neuralbit.letsnote.repos.LabelFireRepo
import com.neuralbit.letsnote.repos.NoteFireRepo
import com.neuralbit.letsnote.repos.TagFireRepo

class MainActivityViewModel(application : Application) : AndroidViewModel(application)  {
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()


    fun getAllFireNotes () : LiveData<ArrayList<NoteFire>>{
        return noteFireRepo.getAllNotes();
    }

    fun allFireTags() : LiveData<List<TagFire>> {
        return tagFireRepo.getAllTags()
    }

    fun allFireLabels() : LiveData<List<LabelFire>> {
        return labelFireRepo.getAllLabels()
    }

    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        noteFireRepo.deleteNote(noteUid)
        tagFireRepo.deleteNoteFromTags(tagList,noteUid)
        labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
    }

}