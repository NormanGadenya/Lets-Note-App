package com.neuralbit.letsnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.neuralbit.letsnote.entities.Note

class LabelNotesActivity : AppCompatActivity() ,NoteClickInterface{
    private lateinit var viewModel : LabelNotesViewModel
    private lateinit var recyclerView : RecyclerView
    val TAG = "LabelNotesActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(LabelNotesViewModel::class.java)
        setContentView(R.layout.activity_label_notes)
        recyclerView = findViewById(R.id.notesRV)
        val labelID = intent.getIntExtra("labelID",1)
        val layoutManager = StaggeredGridLayoutManager( 2, LinearLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        val noteRVAdapter = NoteRVAdapter(applicationContext,this)
        noteRVAdapter.labelViewModel = viewModel
        noteRVAdapter.lifecycleScope = lifecycleScope
        noteRVAdapter.lifecycleOwner = this
        viewModel.getNotesWithLabel(labelID).observe(this){
            var notes = ArrayList<Note>()
            for(i in it){
                notes.add(i.notes)
            }
            noteRVAdapter.updateList(notes)

        }
        recyclerView.adapter= noteRVAdapter
    }


    override fun onNoteClick(note: Note) {
        val intent = Intent( applicationContext, AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteID",note.noteID)

        startActivity(intent)
    }
}