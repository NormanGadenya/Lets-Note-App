package com.neuralbit.letsnote

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class TagRVAdapter (
    val context: Context,
    private val tagRVInterface :TagRVInterface

    ) : RecyclerView.Adapter<TagRVAdapter.ViewHolder>(){
    var deleteIgnored = false
    private val allTags = ArrayList<Tag>()
    val TAG = " TAG "



    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        val tagTitle : TextView = itemView.findViewById(R.id.tagTitle)
        val deleteBtn : ImageButton = itemView.findViewById(R.id.tagDelBtn)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tag_adapter_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tag = allTags[position]
        val tagTitle = tag.tagTitle

        holder.tagTitle.text = context.getString(R.string.tagTitle,tagTitle)
        holder.itemView.setOnLongClickListener {
            holder.deleteBtn.visibility = VISIBLE
            return@setOnLongClickListener true
        }
        holder.deleteBtn.setOnClickListener {
            tagRVInterface.deleteTag(tag)
        }
        Log.d(TAG, "onBindViewHolder: $deleteIgnored")

        if(deleteIgnored){
            holder.deleteBtn.visibility = GONE
        }

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
interface TagRVInterface{
    fun deleteTag(tag : Tag)


}