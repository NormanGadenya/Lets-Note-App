package com.neuralbit.letsnote

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TagRVAdapter (val context: Context) : RecyclerView.Adapter<TagRVAdapter.ViewHolder>(){

    private val allTags = ArrayList<Tag>()
    val TAG = " TAG "

    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        val tagTitle : TextView = itemView.findViewById(R.id.tagTitle)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tag_adapter_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: $allTags")
        val tagTitle = allTags[position].tagTitle
        holder.tagTitle.text = context.getString(R.string.tagTitle,tagTitle)
    }

    override fun getItemCount(): Int {
        return allTags.size
    }

    fun updateList( newList: ArrayList<Tag>){
        allTags.clear()
        allTags.addAll(newList)
        notifyDataSetChanged()
    }

}