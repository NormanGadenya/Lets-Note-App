package com.neuralbit.letsnote.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.neuralbit.letsnote.firebaseEntities.NoteFire
import com.neuralbit.letsnote.firebaseEntities.TodoItem
import com.neuralbit.letsnote.firebaseRepos.DeleteDataRepo
import com.neuralbit.letsnote.firebaseRepos.LabelFireRepo
import com.neuralbit.letsnote.firebaseRepos.NoteFireRepo
import com.neuralbit.letsnote.firebaseRepos.TagFireRepo
import com.neuralbit.letsnote.room.NoteDatabase
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef
import com.neuralbit.letsnote.room.relationships.TagsWithNote
import com.neuralbit.letsnote.room.repos.*

class MainActivityViewModel(application : Application) : AndroidViewModel(application)  {
    private val noteFireRepo : NoteFireRepo = NoteFireRepo()
    var useLocalStorage = false
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

    fun getTagsWithNote(noteUid: String) : LiveData<List<TagsWithNote>>{
        return noteTagRoomRepo.getTagsWithNote(noteUid)
    }


    suspend fun getAllFireNotes () : LiveData<ArrayList<NoteFire>>{
        if (useLocalStorage){
            return noteRoomRepo.allNotes.map {
                val mappedNotes = it.map { note ->
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
//                    for (tagsWithNote in noteTagRoomRepo.getTagsWithNote(note.noteUid)) {
//                        for (t in tagsWithNote.tags) {
//                            tagsList.add(t.tagTitle)
//                        }
//                    }
                    noteFire.tags = tagsList

                    //TODO fix this
                    val todoItems = noteRoomRepo.getTodoList(note.noteUid).value
                    if (todoItems != null){
                        val items = todoItems.map { t -> TodoItem(item = t.itemDesc, checked = t.itemChecked) }
                        noteFire.todoItems = items
                    }
                    return@map noteFire
                }
                return@map ArrayList(mappedNotes)
            }
        }else{
            return noteFireRepo.getAllNotes()
        }
    }


    fun deleteNote (noteUid : String, labelColor : Int, tagList : List<String> ){
        if (useLocalStorage) {
            val reminder = reminderRoomRepo.fetchReminder(noteUid)
            if (reminder.value != null) {
                reminderRoomRepo.delete(noteUid)
            }
            val archivedNote = noteRoomRepo.getArchivedNote(noteUid).value
            if (archivedNote != null) {
                noteRoomRepo.deleteArchive(archivedNote)
            }

            val pinnedNote = noteRoomRepo.getPinnedNote(noteUid).value
            if (pinnedNote != null) {
                noteRoomRepo.deletePinned(pinnedNote)
            }

            val deleted = noteRoomRepo.getDeletedNote(noteUid).value
            if (deleted != null) {
                noteRoomRepo.restoreDeletedNote(deleted)
            }
            val protectedNote = noteRoomRepo.getProtectedNote(noteUid).value
            if (protectedNote != null) {
                noteRoomRepo.deleteProtected(protectedNote)
            }

            for (tag in tagList) {
                val noteTagCrossRef = NoteTagCrossRef(tagTitle = tag, noteUid = noteUid)
                noteTagRoomRepo.deleteNoteTagCrossRef(noteTagCrossRef)
            }
            noteRoomRepo.getTodoList(noteUid).value?.forEach { todo ->
                noteRoomRepo.deleteTodo(todo)
            }
            noteRoomRepo.delete(noteUid)

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