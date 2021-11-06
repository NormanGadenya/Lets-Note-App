package com.neuralbit.letsnote

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteRVAdapter (
    val context: Context,
    val noteClickInterface :NoteClickInterface,
    val noteDeleteInterface :NoteDeleteInterface
    ): RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(){
    private val allNotes = ArrayList<Note>()
        inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
            val noteTitleTV: TextView = itemView.findViewById<TextView>(R.id.tvNoteTitle)
            val noteTextTV: TextView = itemView.findViewById<TextView>(R.id.tvNoteDesc)
            val deleteIV: ImageView = itemView.findViewById<ImageView>(R.id.IvDelete)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var title = allNotes.get(position).title
        var desc = allNotes.get(position).description
        if (title.length >20 ){
            title = title.substring(0,15)+"..."
        }
         if ( desc.length > 250){
             desc= desc.substring(0,250) + "..."
         }

        holder.noteTitleTV.text = title
        holder.noteTextTV.text = desc
        holder.deleteIV.setOnClickListener{
            noteDeleteInterface.onDeleteIconClick(allNotes.get(position))
        }
        holder.itemView.setOnClickListener{
            noteClickInterface.onNoteClick(allNotes.get(position))
        }
    }

    override fun getItemCount(): Int {
        return allNotes.size
    }

    fun updateList( newList: List<Note>){
        allNotes.clear()
        allNotes.addAll(newList)
        notifyDataSetChanged()
    }
}

interface NoteDeleteInterface {
    fun onDeleteIconClick(note:Note)
}

interface  NoteClickInterface{
    fun onNoteClick(note:Note)
}