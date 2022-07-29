package com.neuralbit.letsnote.ui.deletedNotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class DeletedNotesViewModel (application: Application): AndroidViewModel(application) {

    var searchQuery : MutableLiveData<String> = MutableLiveData()



}