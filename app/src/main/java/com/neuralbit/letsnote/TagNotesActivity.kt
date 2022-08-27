package com.neuralbit.letsnote

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.neuralbit.letsnote.Services.DeleteReceiver
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.utilities.AlertReceiver
import java.util.*

class TagNotesActivity : AppCompatActivity() , NoteFireClick {
    private lateinit var noteRVAdapter: NoteRVAdapter
    private lateinit var viewModel : TagNotesViewModel
    private lateinit var allNotesViewModel: AllNotesViewModel
    private lateinit var recyclerView : RecyclerView
    private var actionMode : ActionMode? = null

    val TAG = "TagNotesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(TagNotesViewModel::class.java)
        allNotesViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(AllNotesViewModel::class.java)
        setContentView(R.layout.activity_label_notes)
        recyclerView = findViewById(R.id.notesRV)
        val tagTitle = intent.getStringExtra("tagTitle")
        val noteUids = intent.getStringArrayListExtra("noteUids")



        val layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        noteRVAdapter = NoteRVAdapter(applicationContext,this)
        noteRVAdapter.viewModel = allNotesViewModel
        noteRVAdapter.lifecycleScope = lifecycleScope
        noteRVAdapter.lifecycleOwner = this
        supportActionBar?.title = tagTitle
        if (noteUids != null){
            viewModel.noteUids = noteUids
        }
        val settingsSharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        val fontStyle = settingsSharedPref?.getString("font",null)
        noteRVAdapter.fontStyle = fontStyle
        val staggeredLayoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredLayoutManagerAll
        allNotesViewModel.deleteFrag.value = false
        val staggered = settingsSharedPref?.getBoolean("staggered",true)
        if (staggered == true){
            recyclerView.layoutManager = staggeredLayoutManagerAll
        }else{
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        }
//        allNotesViewModel.getAllFireNotes().observe(this){
//            val notes = ArrayList<NoteFire>()
//            for(note in it){
//                for (uid in viewModel.noteUids){
//                    if (note.noteUid == uid ){
//                        notes.add(note)
//                    }
//                }
//            }
//            viewModel.allTagNotes = notes
//            noteRVAdapter.updateListFire(notes)
//        }

        allNotesViewModel.itemSelectEnabled.observe(this){
            if (it){
                actionMode = startSupportActionMode(MActionModeCallBack())
            }else{
                actionMode?.finish()

            }
        }
        allNotesViewModel.getAllFireNotes().observe(this){
            val pref = applicationContext?.getSharedPreferences("DeletedNotes", MODE_PRIVATE)
            val deletedNotes = pref?.getStringSet("noteUids", HashSet())
            val notes = ArrayList<NoteFire>()
            for(note in it){
                for (uid in viewModel.noteUids){
                    if (note.noteUid == uid && !note.archived  && deletedNotes?.contains(uid) == false){
                        notes.add(note)
                    }
                }
            }
            viewModel.allTagNotes = notes
            noteRVAdapter.updateListFire(notes)
        }


        viewModel.searchQuery.observe(this){

            if (it!=null){
                noteRVAdapter.updateListFire(filterNotes(it))
                noteRVAdapter.searchString = it

            }
        }

        recyclerView.adapter= noteRVAdapter
    }

    private fun filterNotes(text : String) : ArrayList<NoteFire>{
        val newList = ArrayList<NoteFire>()

        return run {
            val textLower= text.toLowerCase(Locale.ROOT)
            for ( note in viewModel.allTagNotes){

                if(note.title.lowercase(Locale.ROOT).contains(textLower) || note.description.toLowerCase(Locale.ROOT)
                        .contains(textLower)
                ){
                    newList.add(note)
                }
            }

            newList
        }
    }

    private inner class MActionModeCallBack : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.action_menu,menu)
            mode?.title = "Delete or Archive notes"
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val restoreItem = menu?.findItem(R.id.restore)
            val archiveItem = menu?.findItem(R.id.archive)
            archiveItem?.isVisible = true
            restoreItem?.isVisible = false

            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            if (item?.itemId == R.id.archive ){
                archiveNotes()
                mode?.finish()
                return true
            }else if (item?.itemId == R.id.delete){

                val alertDialog: AlertDialog.Builder = AlertDialog.Builder(applicationContext)
                alertDialog.setTitle("Are you sure about this ?")
                alertDialog.setPositiveButton("Yes"
                ) { _, _ ->
                    deleteNotes()
                    mode?.finish()
                }
                alertDialog.setNegativeButton("Cancel"
                ) { dialog, _ ->
                    run {
                        mode?.finish()
                        dialog.cancel()
                    }
                }
                alertDialog.show()


                return true

            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            allNotesViewModel.itemSelectEnabled.value = false
            viewModel.selectedNotes.clear()
            actionMode = null
        }

    }

    private fun deleteNotes() {
        if (allNotesViewModel.selectedNotes.isNotEmpty()){
            val pref = applicationContext?.getSharedPreferences("DeletedNotes", MODE_PRIVATE)
            val settings = applicationContext?.getSharedPreferences("Settings", MODE_PRIVATE)
            val emptyTrashImmediately = settings?.getBoolean("EmptyTrashImmediately",false)

            for ( note in allNotesViewModel.selectedNotes){
                val editor: SharedPreferences.Editor ?= pref?.edit()
                val noteUids = pref?.getStringSet("noteUids",HashSet())
                val deletedNoteUids = HashSet<String>()
                if (noteUids != null){ deletedNoteUids.addAll(noteUids)}

                if (emptyTrashImmediately != true){
                    editor?.putLong(note.noteUid,System.currentTimeMillis())

                    note.noteUid?.let { it1 -> deletedNoteUids.add(it1) }

                    note.noteUid?.let { it1 -> scheduleDelete(it1,note.tags,note.label,note.timeStamp) }

                    editor?.putStringSet("noteUids",deletedNoteUids)
                    editor?.apply()
                }else{
                    viewModel.allTagNotes.remove(note)
                    if (emptyTrashImmediately){
                        note.noteUid?.let { viewModel.deleteNote(it,note.label,note.tags) }
                        allNotesViewModel.selectedNotes.clear()
                    }
                }
                viewModel.allTagNotes.remove(note)
                noteRVAdapter.updateListFire(viewModel.allTagNotes)
                noteRVAdapter?.notifyDataSetChanged()


            }

            if (emptyTrashImmediately != true){
                allNotesViewModel.selectedNotes.clear()
            }


            allNotesViewModel.itemSelectEnabled.value = false

            Toast.makeText(applicationContext,"Notes deleted successfully", Toast.LENGTH_SHORT).show()
        }

    }

    private fun cancelAlarm(reminder : Int){
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, reminder, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }


    private fun scheduleDelete( noteUid : String, tags : List<String>, label: Int , timeStamp : Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, DeleteReceiver::class.java)

        intent.putExtra("noteUid",noteUid)
        intent.putExtra("timeStamp",System.currentTimeMillis())
        intent.putExtra("labelColor", label)
        intent.putStringArrayListExtra("tagList", ArrayList(tags))
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, timeStamp.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
        val timeToDelete = timeStamp + 6.048e+8 // 7 days from the time it is softly deleted
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeToDelete.toLong(), pendingIntent)
    }

    private fun archiveNotes() {
        if (allNotesViewModel.selectedNotes.isNotEmpty()){

            for ( note in allNotesViewModel.selectedNotes){
                viewModel.allTagNotes.remove(note)
                cancelAlarm(note.reminderDate.toInt())

                val noteUpdate = HashMap<String,Any>()
                noteUpdate["archived"] = true
                note.noteUid?.let { it1 -> allNotesViewModel.updateFireNote(noteUpdate, it1) }
            }
            noteRVAdapter.updateListFire(viewModel.allTagNotes)
            noteRVAdapter.notifyDataSetChanged()

            allNotesViewModel.selectedNotes.clear()

            allNotesViewModel.itemSelectEnabled.value = false

            Toast.makeText(applicationContext,"Notes archived successfully", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.label_tag_activity_menu, menu)
        val searchViewMenuItem = menu.findItem(R.id.search)
        val searchView = searchViewMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                if (p0 != null) {
                    viewModel.searchQuery.value = p0
                }
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    viewModel.searchQuery.value = p0

                }
                return false
            }
        })

        return true
    }

    override fun onNoteFireClick(note: NoteFire , activated : Boolean) {
        if (!note.selected && !activated){
            val intent : Intent = if(note.protected){
                Intent( applicationContext, Fingerprint::class.java)
            }else{
                Intent( applicationContext, AddEditNoteActivity::class.java)
            }
            intent.putExtra("noteType","Edit")
            intent.putExtra("noteTitle",note.title)
            intent.putExtra("noteDescription",note.description)
            intent.putExtra("noteUid",note.noteUid)
            intent.putExtra("timeStamp",note.timeStamp)
            intent.putExtra("labelColor",note.label)
            intent.putExtra("pinned",note.pinned)
            intent.putExtra("archieved",note.archived)
            intent.putExtra("protected",note.protected)
            val c = Calendar.getInstance()
            if (c.timeInMillis < note.reminderDate){
                intent.putExtra("reminder",note.reminderDate)
            }
            val toDoItemString: String = Gson().toJson(note.todoItems)
            intent.putExtra("todoItems", toDoItemString)
            intent.putStringArrayListExtra("tagList", ArrayList(note.tags))
            startActivity(intent)
        }else{
            if (note.selected){
                viewModel.selectedNotes.add(note)
            }else{
                viewModel.selectedNotes.remove(note)
            }
        }
    }

    override fun onNoteFireLongClick(note: NoteFire) {
        if (note.selected){
            viewModel.selectedNotes.add(note)
        }else{
            viewModel.selectedNotes.remove(note)
        }
        allNotesViewModel.itemSelectEnabled.value = true
    }

}