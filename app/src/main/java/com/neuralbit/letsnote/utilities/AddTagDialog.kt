package com.neuralbit.letsnote.utilities

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatDialogFragment
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.Tag

class AddTagDialog(val getTagFromDialog: GetTagFromDialog): AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val layoutInflater = activity?.layoutInflater
        val view = layoutInflater?.inflate(R.layout.add_tag_dialog,null)
        val addTagET = view?.findViewById<EditText>(R.id.newTagET)

        builder.setView(view)
            .setTitle("Add new tag")
            .setNegativeButton("cancel"
            ) { p0, p1 -> TODO("Not yet implemented") }
            .setPositiveButton("ok"){p0, p1 ->
                run {
                    var tagTitle = addTagET?.text.toString()
                    if(tagTitle[0]=='#'){
                        tagTitle= tagTitle.substring(1,tagTitle.length)
                    }
                    getTagFromDialog.getTag(Tag(tagTitle))
                }
            }
        return builder.create()
   }
}

interface GetTagFromDialog{
    fun getTag(tag: Tag)
}