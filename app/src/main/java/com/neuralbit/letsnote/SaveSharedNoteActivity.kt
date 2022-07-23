package com.neuralbit.letsnote

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.neuralbit.letsnote.repos.NoteFireIns
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class SaveSharedNoteActivity : AppCompatActivity() {
    private var noteDescEdit : EditText? = null
    private var noteTitleEdit : EditText? = null
    private var noteDesc : String? = null
    private var noteTitle : String? = null
    private lateinit var viewModal : NoteViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModal = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)


        if(intent?.action == Intent.ACTION_SEND){
            if("text/plain" == intent.type){
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    noteDesc = it
                }
            }
        }
        val layoutView = layoutInflater.inflate(R.layout.save_shared_note_fragment,null)

        val saveNoteDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setView(layoutView)
            }
            builder.create()
        }
        saveNoteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        saveNoteDialog.setOnDismissListener {
            dismissApp()
        }
        noteDescEdit = layoutView.findViewById(R.id.noteEditDesc)
        noteTitleEdit = layoutView.findViewById(R.id.noteEditTitle)
        val cancelBtn = layoutView.findViewById<Button>(R.id.cancelButton)
        val saveNoteBtn = layoutView.findViewById<Button>(R.id.saveNoteButton)
        cancelBtn.setOnClickListener {
            dismissApp()
        }
        saveNoteBtn.setOnClickListener {
            noteDesc = noteDescEdit?.text.toString()
            noteTitle = noteTitleEdit?.text.toString()
            if(noteDesc!=null || noteTitle!=null){
                GlobalScope.launch {
                    withContext(Dispatchers.IO){
                        saveNote()

                    }
                }
            }
        }

        noteDescEdit?.setText(noteDesc)
        saveNoteDialog.show()
    }

    private suspend fun saveNote() {
        val cm = Common()
        viewModal.addFireNote(NoteFireIns(noteTitle,noteDesc, timeStamp = cm.currentTimeToLong()))
        dismissApp()

    }

    private fun dismissApp(){
        finishAffinity()
        exitProcess(0)
    }

}