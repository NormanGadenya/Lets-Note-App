package com.neuralbit.letsnote.utilities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.Tag


class AddTagDialog(val getTagFromDialog: GetTagFromDialog,val ctx: Context): AppCompatDialogFragment() {
    var tagList : ArrayList<String> = ArrayList()
    val TAG = "AddTagDialog"
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity)
        val layoutInflater = activity?.layoutInflater
        val view = layoutInflater?.inflate(R.layout.add_tag_dialog,null)
        val addTagET = view?.findViewById<AutoCompleteTextView>(R.id.newTagET)

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(ctx, android.R.layout.select_dialog_item, tagList)
        addTagET?.setAdapter(adapter)
        addTagET?.threshold = 1
        builder.setView(view)
            .setTitle("Add new tag")
            .setNegativeButton("cancel"
            ) { _, _ -> dismiss() }
            .setPositiveButton("ok"){_, _ ->
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