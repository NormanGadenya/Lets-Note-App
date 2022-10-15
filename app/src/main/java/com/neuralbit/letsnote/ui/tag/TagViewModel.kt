package com.neuralbit.letsnote.ui.tag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.firebase.entities.TagFire

class TagViewModel (application: Application): AndroidViewModel(application) {
    var allTags  = ArrayList<Tag>()
    var allTagFire : List<TagFire>  = ArrayList()
    var noteUids = ArrayList<String>()
    var searchQuery : MutableLiveData <String> = MutableLiveData()
    var staggeredView : MutableLiveData<Boolean> = MutableLiveData()
}