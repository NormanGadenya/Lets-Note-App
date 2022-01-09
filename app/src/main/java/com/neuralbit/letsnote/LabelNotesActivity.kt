package com.neuralbit.letsnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.adapters.NoteClickInterface
import com.neuralbit.letsnote.adapters.NoteRVAdapter
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel

class LabelNotesActivity : AppCompatActivity() , NoteClickInterface {
    private lateinit var viewModel : LabelNotesViewModel
    private lateinit var allNotesViewModel: AllNotesViewModel
    private lateinit var notesList : ArrayList<Note>
    private lateinit var recyclerView : RecyclerView
    val TAG = "LabelNotesActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(LabelNotesViewModel::class.java)
        allNotesViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(AllNotesViewModel::class.java)
        supportActionBar?.title = "Label"
        setContentView(R.layout.activity_label_notes)
        recyclerView = findViewById(R.id.notesRV)
        val labelID = intent.getIntExtra("labelID",1)
        val layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        val noteRVAdapter = NoteRVAdapter(applicationContext,this)
        noteRVAdapter.viewModel = allNotesViewModel
        noteRVAdapter.lifecycleScope = lifecycleScope
        noteRVAdapter.lifecycleOwner = this
//        viewModel.getNotesWithLabel(labelID).observe(this){
//            notesList= ArrayList()
//            for(i in it){
//                notesList.add(i.notes)
//            }
//            noteRVAdapter.updateList(notesList)
//
//        }

        viewModel.searchQuery.observe(this){

            if (it!=null){
                noteRVAdapter.updateList(filterNotes(it))
                noteRVAdapter.searchString = it

            }
        }
        recyclerView.adapter= noteRVAdapter
    }

    fun filterNotes(text : String) : ArrayList<Note>{
        val newList = ArrayList<Note>()

        return if(text!=null){
            val textLower= text.toLowerCase()
            for ( note in notesList){

                if(note.title?.toLowerCase()?.contains(textLower) == true || note.description?.toLowerCase()
                        ?.contains(textLower) == true
                ){
                    newList.add(note)
                }
            }

            newList
        }else{
            notesList
        }
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent( applicationContext, AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteID",note.noteID)

        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.labelnotes_activity, menu)
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

}