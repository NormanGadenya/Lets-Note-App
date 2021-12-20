package com.neuralbit.letsnote

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TagRVAdapter (
    val context: Context,
    val tagRVInterface :TagRVInterface
    ) : RecyclerView.Adapter<TagRVAdapter.ViewHolder>(){

    private val allTags = ArrayList<Tag>()
    val TAG = " TAG "
    private val selectedTags = ArrayList<Tag>()
    private var lastSelectedPosition = -1
    private val tagStatus = HashMap<String,Boolean>()


    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        val tagTitle : TextView = itemView.findViewById(R.id.tagTitle)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tag_adapter_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: $allTags")
        val tag = allTags[position]
        val tagTitle = tag.tagTitle

        holder.tagTitle.text = context.getString(R.string.tagTitle,tagTitle)
        holder.itemView.setOnClickListener {
//            if(lastSelectedPosition > 0){
//                selectedTags.remove(tagTitle)
//            }
            if(selectedTags.contains(tag)){
                selectedTags.remove(tag)
            }else{
                selectedTags.add(tag)
            }
            if(selectedTags.contains(tag)){
                holder.itemView.setBackgroundResource(R.drawable.tag_background_sel)
                holder.tagTitle.setTextColor(context.getColor(R.color.white))

            }else{
                holder.itemView.setBackgroundResource(R.drawable.tag_background)
                holder.tagTitle.setTextColor(context.getColor(R.color.black))

            }
            tagRVInterface.selectedTags(selectedTags)

//            lastSelectedPosition = holder.adapterPosition
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
    fun selectedTags(selectedTags : List<Tag>)


}