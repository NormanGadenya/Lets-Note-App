package com.neuralbit.letsnote.ui.tag

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.TagNotesActivity
import com.neuralbit.letsnote.utilities.Common

class TagRVAdapter (
    val context: Context
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
        if(noteCount==1){
            holder.tagNoteCount.text = context.getString(R.string.noteTagCountSingle,noteCount.toString())
        }else {
            holder.tagNoteCount.text =
                context.getString(R.string.noteTagCountMultiple, noteCount.toString())
        }
        holder.tagCard.setOnClickListener {
            val intent = Intent(context, TagNotesActivity::class.java)
            intent.putExtra("tagTitle",tagTitle)
            context.startActivity(intent)
        }
        searchString?.let {
            cm.setHighLightedText(holder.tagNameTV, it)

        }
    }

    override fun getItemCount(): Int {
        return tagList.size
    }

    fun updateTagList(tList : ArrayList<Tag>){
        tagList = tList
        notifyDataSetChanged()
    }
}
