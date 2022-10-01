package com.neuralbit.letsnote.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseEntities.TodoItem
import com.neuralbit.letsnote.firebaseRepos.DeleteDataRepo
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.repos.*

class MainActivityViewModel(application : Application) : AndroidViewModel(application)  {
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    val useLocalStorage = false
    private val labelFireRepo : LabelFireRepo = LabelFireRepo()
    private val tagFireRepo : TagFireRepo = TagFireRepo()
    private val fUser = FirebaseAuth.getInstance().currentUser
    private val deleteDataRepo = DeleteDataRepo()
    private val noteRoomDao = NoteDatabase.getDatabase(application).getNotesDao()
    private val noteRoomRepo = NoteRoomRepo(noteRoomDao)

    private val labelRoomDao = NoteDatabase.getDatabase(application).getLabelDao()
    private val labelRoomRepo = LabelRoomRepo(labelRoomDao)

    private val tagRoomDao = NoteDatabase.getDatabase(application).getTagDao()
    private val tagRoomRepo = TagRoomRepo(tagRoomDao)

    private val reminderRoomDao = NoteDatabase.getDatabase(application).getReminderDao()
    private val reminderRoomRepo = ReminderRoomRepo(reminderRoomDao)

    private val noteTagRoomDao = NoteDatabase.getDatabase(application).getNoteTagDao()
    private val noteTagRoomRepo = NoteTagRoomRepo(noteTagRoomDao)

    suspend fun getAllFireNotes () : LiveData<ArrayList<NoteFire>>{
        if (useLocalStorage){
            val notes = noteRoomDao.getAllNotes().value
            val mappedNotes = notes?.map { it ->
                val noteFire = NoteFire()
                noteFire.noteUid = it.noteUid
                noteFire.description = it.description
                noteFire.title = it.title
                noteFire.timeStamp = it.timestamp
                noteFire.label = it.labelColor
                val reminder = reminderRoomRepo.fetchReminder(it.noteUid)
                if (reminder.value != null){
                    noteFire.reminderDate = reminder.value!!.time
                }
                val archivedNote = noteRoomRepo.getArchivedNote(it.noteUid)
                if (archivedNote.value != null){
                    noteFire.archived = true
                }

                val pinnedNote = noteRoomRepo.getPinnedNote(it.noteUid)
                if (pinnedNote.value != null){
                    noteFire.pinned = true
                }

                val deleted = noteRoomRepo.getDeletedNote(it.noteUid)
                if (deleted.value != null){
                    noteFire.deletedDate = deleted.value!!.timestamp
                }

                val protectedNote = noteRoomRepo.getProtectedNote(it.noteUid)
                if (protectedNote.value != null){
                    noteFire.protected = true
                }
                val tagRoomList = noteTagRoomRepo.getTagsWithNote(it.noteUid)
                for (t in tagRoomList) {
                    val tagSList = ArrayList<String>()
                    for (tag in t.tags) {
                        tagSList.add(tag.tagTitle)
                    }
                    noteFire.tags = tagSList
                }
                val todoItems = noteRoomRepo.getTodoList(it.noteUid).value
                if (todoItems != null){
                    val items = todoItems.map { t -> TodoItem(item = t.itemDesc, checked = t.itemChecked) }
                    noteFire.todoItems = items
                }
                return@map noteFire
            }
            return MutableLiveData(mappedNotes?.let { ArrayList(it) })
        }else{
            return noteFireRepo.getAllNotes()
        }
    }


    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        if (useLocalStorage){
//            noteRoomRepo.dele
        }else{

            noteFireRepo.deleteNote(noteUid)
            tagFireRepo.deleteNoteFromTags(tagList,noteUid)
            labelFireRepo.deleteNoteFromLabel(labelColor,noteUid)
        }
    }

    fun updateFireNote(noteUpdate : Map<String, Any>, noteUid : String) {
        noteFireRepo.updateNote(noteUpdate,noteUid)
    }

    fun deleteUserDataContent(context : Context){
        deleteDataRepo.deleteUserData(context)
    }

}