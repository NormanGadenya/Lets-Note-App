package com.neuralbit.letsnote.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neuralbit.letsnote.repos.MigrationRepo

class SettingsViewModel : ViewModel() {
    var settingsFrag = MutableLiveData<Boolean>()
    var migrateData = MutableLiveData<Boolean>()
    var migrationRepo = MigrationRepo()
    var oldUser: String? = null
    var newUser: String? = null

    fun migrateData(oldUser:String, newUser:String){
        migrationRepo.migrateData(oldUser, newUser)
    }

}