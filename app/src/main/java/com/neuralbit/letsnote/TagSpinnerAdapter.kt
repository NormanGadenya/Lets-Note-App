package com.neuralbit.letsnote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class TagSpinnerAdapter (context: Context , list: List<Tag>) :
    ArrayAdapter<Tag>(context,0,list){
    val layoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view : View = layoutInflater.inflate(R.layout.tag_spinner_row,null,true)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        if(cv == null){
            cv = layoutInflater.inflate(R.layout.tag_spinner_row,parent,false)
        }
        return view(cv!!,position)
    }

    private fun view(cv: View, position: Int): View {
        val tagItem : Tag = getItem(position) ?: return cv
        val tagTitle = cv.findViewById<TextView>(R.id.tagTitle)
        tagTitle.text = tagItem.tagTitle
        return cv
    }


}
