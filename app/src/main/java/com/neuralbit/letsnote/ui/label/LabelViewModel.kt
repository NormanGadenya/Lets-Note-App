package com.neuralbit.letsnote.ui.label

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.neuralbit.letsnote.entities.LabelFire
import com.neuralbit.letsnote.repos.LabelFireRepo

class LabelViewModel (application: Application): AndroidViewModel(application)  {
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    var labelFire : List<LabelFire> = ArrayList()

}