package com.neuralbit.letsnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.entities.NoteFire

class TagNotesViewModel(
    application: Application) :AndroidViewModel(application){
    val searchQuery: MutableLiveData<String>
    var noteUids = ArrayList<String>()
    var allTagNotes = ArrayList<NoteFire>()

    init {
        searchQuery = MutableLiveData()
    }


}