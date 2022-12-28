package com.neuralbit.letsnote.ui.addEditNote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R

class AddEditTagRVAdapter (
    val context: Context,
    private val tagRVInterface : TagRVInterface

    ) : RecyclerView.Adapter<AddEditTagRVAdapter.ViewHolder>(){
    var deleteIgnored = false
    private val allTags = ArrayList<String>()
    var lifecycleOwner : LifecycleOwner ? = null
    var viewModel : NoteViewModel ? = null
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
        var tagTitle = tag
        if (tagTitle[0] != '#'){
            tagTitle = "#$tagTitle"
        }
        holder.tagTitle.text = tagTitle
        holder.itemView.setOnLongClickListener {

            holder.deleteBtn.visibility = VISIBLE
            return@setOnLongClickListener true
        }
        holder.deleteBtn.setOnClickListener {
            holder.deleteBtn.visibility = GONE
            tagRVInterface.deleteTag(tag)
        }
        lifecycleOwner?.let {
            viewModel?.deleteIgnored?.observe(it){ d ->
                if (d){
                    holder.deleteBtn.visibility = GONE
                }
            }
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