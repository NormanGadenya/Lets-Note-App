package com.neuralbit.letsnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.entities.NoteFire

class LabelNotesViewModel(
    application: Application) :AndroidViewModel(application){
    val searchQuery: MutableLiveData<String>
    var labelNotes = ArrayList<NoteFire>()
    var noteUids = ArrayList<String>()

    init {
        searchQuery = MutableLiveData()
    }

}