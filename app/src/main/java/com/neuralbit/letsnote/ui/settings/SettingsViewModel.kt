package com.neuralbit.letsnote.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neuralbit.letsnote.firebase.repos.MigrationRepo

class SettingsViewModel : ViewModel() {
    val dataMigrated: MutableLiveData<Boolean> = MutableLiveData()
    var settingsFrag = MutableLiveData<Boolean>()
    private var migrationRepo = MigrationRepo()


    fun migrateData(oldUser:String, newUser:String){
        migrationRepo.migrateData(oldUser, newUser)
    }

}