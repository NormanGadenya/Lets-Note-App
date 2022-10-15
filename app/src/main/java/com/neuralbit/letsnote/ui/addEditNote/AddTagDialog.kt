package com.neuralbit.letsnote.ui.addEditNote

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.utilities.CamelCaseConverter


class AddTagDialog(private val getTagFromDialog: GetTagFromDialog, val ctx: Context): AppCompatDialogFragment() {
    var tagList : ArrayList<String> = ArrayList()
    val TAG = "AddTagDialog"
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(ctx)
        val layoutInflater = activity?.layoutInflater
        val view = layoutInflater?.inflate(R.layout.add_tag_dialog,null)
        val addTagET = view?.findViewById<AutoCompleteTextView>(R.id.newTagET)
        addTagET?.addTextChangedListener( object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                var tagTyped = p0.toString()
                if (tagTyped.isNotEmpty()){
                    if (tagTyped[0] != '#'){
                        tagTyped = "#${tagTyped}"
                        addTagET.setText(tagTyped)
                        addTagET.setSelection(tagTyped.length)
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(ctx, android.R.layout.select_dialog_item, tagList)
        addTagET?.setAdapter(adapter)
        addTagET?.threshold = 1
        builder.setView(view)
            .setTitle("Add new tag")
            .setNegativeButton("Cancel"
            ) { _, _ -> dismiss() }
            .setPositiveButton("Ok"){_, _ ->
                run {

                    var tagTitle = addTagET?.text.toString()
                    if (tagTitle.isNotEmpty()){

                        if (tagTitle[0] == '#'){
                            tagTitle = tagTitle.substring(1,tagTitle.length)
                        }
                        tagTitle = CamelCaseConverter.convertString(tagTitle)


                        getTagFromDialog.getTag(tagTitle)
                    }
                }
            }
        return builder.create()
   }
}

interface GetTagFromDialog{
    fun getTag(tag: String)
}