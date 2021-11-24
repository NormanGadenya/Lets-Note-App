package com.neuralbit.letsnote.ui.tag

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.NoteClickInterface
import com.neuralbit.letsnote.Tag

class TagRVAdapter (
    val context: Context,
    val tagClickInterface: tagClickInterface
        ): RecyclerView.Adapter<TagRVAdapter.ViewHolder>(){
   inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

}

interface tagClickInterface {
    fun onTagItemClick(tag : Tag)
}