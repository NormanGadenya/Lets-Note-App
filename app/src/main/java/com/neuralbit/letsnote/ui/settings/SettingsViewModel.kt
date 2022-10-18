package com.neuralbit.letsnote.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.neuralbit.letsnote.firebase.entities.LabelFire
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.firebase.entities.TagFire
import com.neuralbit.letsnote.firebase.entities.TodoItem
import com.neuralbit.letsnote.firebase.repos.MigrationRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.repos.LabelRoomRepo
import com.neuralbit.letsnote.room.repos.NoteRoomRepo
import com.neuralbit.letsnote.room.repos.NoteTagRoomRepo
import com.neuralbit.letsnote.room.repos.TagRoomRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel  (application : Application) : AndroidViewModel(application) {
    val dataMigrated: MutableLiveData<Boolean> = MutableLiveData()
    var settingsFrag = MutableLiveData<Boolean>()
    private var migrationRepo = MigrationRepo()
    var useLocalStorage = false
    private val noteRoomDao = NoteDatabase.getDatabase(application).getNotesDao()
    private val noteRoomRepo = NoteRoomRepo(noteRoomDao)

    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)

    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()
    private val tagRoomRepo = TagRoomRepo(tagRoomDao)
    private val TAG = "SettingsViewModel"


    fun migrateData(oldUser:String ?, newUser:String) : MutableLiveData<Boolean>{
        val migrationCompleted = MutableLiveData<Boolean>()
        if (oldUser != null ){
            migrationRepo.migrateDataAnonymous(oldUser, newUser)
            migrationCompleted.postValue(true)

        }else{
            viewModelScope.launch(Dispatchers.IO) {

                val noteList = ArrayList<NoteFire>()
                for (note in noteRoomRepo.getAllNotes()) {
                    val noteFire = NoteFire()
                    noteFire.noteUid = note.noteUid
                    noteFire.description = note.description!!
                    noteFire.title = note.title!!
                    noteFire.timeStamp = note.timestamp
                    noteFire.label = note.labelColor
                    noteFire.protected = note.locked
                    noteFire.archived = note.archived
                    noteFire.pinned = note.pinned
                    noteFire.deletedDate = note.deletedDate
                    noteFire.reminderDate = note.reminderDate
                    val tagsList = ArrayList<String>()
                    for (tagsWithNote in noteTagRoomRepo.getTagsWithNote(note.noteUid)) {
                        for (t in tagsWithNote.tags){
                            tagsList.add("#${t.tagTitle}")
                        }
                    }
                    noteFire.tags = tagsList
                    val todoItems = noteRoomRepo.getTodoList(note.noteUid)
                    val items = todoItems.map { t -> TodoItem(item = t.itemDesc, checked = t.itemChecked) }
                    noteFire.todoItems = items
                    noteList.add(noteFire)

                }

                val tagFireList = java.util.ArrayList<TagFire>()
                tagRoomRepo.getAllTags().forEach {
                    val tagList = noteTagRoomRepo.getNotesWithTag(tagTitle = it.tagTitle)
                    val noteUids = java.util.ArrayList<String>()
                    for (notesWithTag in tagList) {
                        for ( n in notesWithTag.notes){
                            noteUids.add(n.noteUid)
                        }
                    }
                    val tag = TagFire()
                    tag.tagName = it.tagTitle
                    tag.noteUids = noteUids
                    tagFireList.add(tag)
                }

                val labelFireList = java.util.ArrayList<LabelFire>()
                labelRoomRepo.getAllLabels().forEach { l ->
                    val noteUids = l.notes.map { note -> note.noteUid }
                    val labelFire = LabelFire()
                    labelFire.labelColor = l.label.labelColor
                    labelFire.noteUids = java.util.ArrayList(noteUids)
                    if (l.label.labelTitle != null){
                        labelFire.labelTitle = l.label.labelTitle
                    }
                    labelFireList.add(labelFire)
                }
                migrationRepo.migrateDataRoom(newUser,noteList,tagFireList,labelFireList)
                // clear database
                noteTagRoomRepo.deleteAllNoteTagCrossRefs()
                tagRoomRepo.deleteAllTags()
                noteRoomRepo.deleteAllNotes()
                labelRoomDao.deleteAllLabels()

                migrationCompleted.postValue(true)

            }

        }
        return migrationCompleted
    }


}