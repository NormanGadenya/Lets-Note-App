package com.neuralbit.letsnote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), NoteClickInterface{
    lateinit var  notesRV: RecyclerView
    lateinit var addFAB : FloatingActionButton
    lateinit var viewModal: NoteViewModel
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notesRV = findViewById(R.id.notesRV)
        addFAB = findViewById(R.id.FABAddNote)
        notesRV.layoutManager = LinearLayoutManager(this)
        val noteRVAdapter = NoteRVAdapter(this,this)
        notesRV.adapter= noteRVAdapter
        var list1 :List<Note> = arrayListOf()
        viewModal = ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(NoteViewModel::class.java)
        viewModal.allNotes.observe(this,{

                Log.d(TAG, "onCreate: x" )
                noteRVAdapter.updateList(it)
                viewModal.notes=it
                viewModal.list= it

        })
        addFAB.setOnClickListener{
            val intent = Intent( this@MainActivity,AddEditNoteActivity::class.java)
            intent.putExtra("noteType","newNote")
            startActivity(intent)
            this.finish()
        }
        viewModal.searchQurery.value = "Hello"
        viewModal.filterList().observe(this,{
            noteRVAdapter.updateList(it)
        })
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent( this@MainActivity,AddEditNoteActivity::class.java)
        intent.putExtra("noteType","Edit")
        intent.putExtra("noteTitle",note.title)
        intent.putExtra("noteDescription",note.description)
        intent.putExtra("noteID",note.id)
        intent.putExtra("noteTimeStamp",note.timeStamp )
        startActivity(intent)
        this.finish()

    }


}