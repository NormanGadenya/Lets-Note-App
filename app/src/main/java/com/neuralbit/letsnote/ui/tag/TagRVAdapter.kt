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
   inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tagTV : TextView = itemView.findViewById(R.id.tagTV)
    }
    private var tagList = ArrayList<Tag>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tag_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tagTV.text = tagList[position].tagTitle
    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    fun updateTagList(tList : ArrayList<Tag>){
        tagList = tList
        notifyDataSetChanged()
    }
}
