package com.neuralbit.letsnote.ui.tag

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.Tag

class TagRVAdapter (
    val context: Context
        ): RecyclerView.Adapter<TagRVAdapter.ViewHolder>(){
    var tagCount : Map<String,Int> = HashMap()

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tagNameTV : TextView = itemView.findViewById(R.id.tagName)
        val tagNoteCount : TextView = itemView.findViewById(R.id.noteCount)
    }
    private var tagList : ArrayList<Tag> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tag_frag_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tagTitle = tagList[position].tagTitle
        holder.tagNameTV.text = context.getString(R.string.tagTitle,tagTitle)
        val noteCount = tagCount[tagTitle]
        if(noteCount==1){
            holder.tagNoteCount.text = context.getString(R.string.noteTagCountSingle,noteCount.toString())
        }else {
            holder.tagNoteCount.text =
                context.getString(R.string.noteTagCountMultiple, noteCount.toString())
        }
    }

    override fun getItemCount(): Int {
        return tagList?.size!!
    }

    fun updateTagList(tList : ArrayList<Tag>){
        tagList = tList
        notifyDataSetChanged()
    }
}
