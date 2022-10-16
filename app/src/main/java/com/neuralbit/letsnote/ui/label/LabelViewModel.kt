package com.neuralbit.letsnote.ui.label

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.neuralbit.letsnote.firebase.entities.LabelFire
import com.neuralbit.letsnote.firebase.repos.LabelFireRepo

class LabelViewModel (application: Application): AndroidViewModel(application)  {
    var labelList: ArrayList<Label>  = ArrayList()
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    var labelFire : List<LabelFire> = ArrayList()
    var searchQuery : MutableLiveData<String> = MutableLiveData()
    var bannerKey : String?= null
    var useLocalStorage = false


}