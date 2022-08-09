package com.neuralbit.letsnote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R

class AddEditTagRVAdapter (
    val context: Context,
    private val tagRVInterface :TagRVInterface

    ) : RecyclerView.Adapter<AddEditTagRVAdapter.ViewHolder>(){
    var deleteIgnored = false
    private val allTags = ArrayList<String>()
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

        holder.tagTitle.text = tag
        holder.itemView.setOnLongClickListener {
            holder.deleteBtn.visibility = VISIBLE
            return@setOnLongClickListener true
        }
        holder.deleteBtn.setOnClickListener {
            tagRVInterface.deleteTag(tag)
        }

        if (deleteIgnored) {
            holder.deleteBtn.visibility = GONE
        }

    }

    override fun getItemCount(): Int {
        return allTags.size
    }

    fun updateList( newList: ArrayList<String>){
        allTags.clear()
        allTags.addAll(newList)
        notifyDataSetChanged()
    }



}
interface TagRVInterface{
    fun deleteTag(tag : String)


}