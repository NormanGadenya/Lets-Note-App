package com.neuralbit.letsnote

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.neuralbit.letsnote.adapters.NoteFireClick
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import java.util.*

class TagNotesActivity : AppCompatActivity() , NoteFireClick {
    private lateinit var viewModel : TagNotesViewModel
    private lateinit var allNotesViewModel: AllNotesViewModel
    private lateinit var recyclerView : RecyclerView
    val TAG = "LabelNotesActivity"

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
        val noteRVAdapter = NoteRVAdapter(applicationContext,this)
        noteRVAdapter.viewModel = allNotesViewModel
        noteRVAdapter.lifecycleScope = lifecycleScope
        noteRVAdapter.lifecycleOwner = this
        supportActionBar?.title = tagTitle
        if (noteUids != null){
            viewModel.noteUids = noteUids
        }
        val settingsSharedPref = getSharedPreferences("Settings", MODE_PRIVATE)
        val staggeredLayoutManagerAll = StaggeredGridLayoutManager( 2,LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredLayoutManagerAll
        allNotesViewModel.deleteFrag.value = false
        val staggered = settingsSharedPref?.getBoolean("staggered",true)
        if (staggered == true){
            recyclerView.layoutManager = staggeredLayoutManagerAll
        }else{
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        }
        allNotesViewModel.getAllFireNotes().observe(this){
            val notes = ArrayList<NoteFire>()
            for(note in it){
                for (uid in viewModel.noteUids){
                    if (note.noteUid == uid ){
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
    }

    override fun onNoteFireLongClick(note: NoteFire) {
        TODO("Not yet implemented")
    }

}