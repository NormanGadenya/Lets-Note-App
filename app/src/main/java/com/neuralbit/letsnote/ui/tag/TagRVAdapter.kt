package com.neuralbit.letsnote.ui.tag

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.*

class TagRVAdapter (
    val context: Context,
    val tagItemClick: TagItemClick
        ): RecyclerView.Adapter<TagRVAdapter.ViewHolder>(){
    var searchString : String? = null

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tagNameTV : TextView = itemView.findViewById(R.id.tagName)
        val tagNoteCount : TextView = itemView.findViewById(R.id.noteCount)
        val tagCard : CardView = itemView.findViewById(R.id.tagCard)
    }

    private var tagList : ArrayList<Tag> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tag_frag_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tagTitle = tagList[position].title
        holder.tagNameTV.text = tagTitle
        val noteCount = tagList[position].noteCount
        val cm = Common()


        GlobalScope.launch {
            updateNoteCountTV(noteCount,holder.tagNoteCount)
        }
        holder.tagCard.setOnClickListener {
            tagItemClick.onTagItemClick(tagTitle)
        }
        searchString?.let {
            cm.setHighLightedText(holder.tagNameTV, it)
        }
    }

    private suspend fun updateNoteCountTV(value : Int, textView: TextView){
        withContext(Dispatchers.Main){
            for (i in 0..value){
                delay(100L)
                if(value==1){
                    textView.text = context.getString(R.string.noteTagCountSingle,value.toString())
                }else {
                    textView.text = context.getString(R.string.noteCountMultiple, value.toString())
                }
            }
        }
    }
    override fun getItemCount(): Int {
        return tagList.size
    }

    fun updateTagList(tList : ArrayList<Tag>){
        tagList = tList
        notifyDataSetChanged()
    }

    interface  TagItemClick{
        fun onTagItemClick(tagTitle: String)
    }
}
