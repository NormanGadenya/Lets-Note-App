package com.neuralbit.letsnote

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.utilities.Common

class NoteRVAdapter (
    val context: Context,
    val noteClickInterface :NoteClickInterface,


    ): RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(){

    private val allNotes = ArrayList<Note>()
    var searchString: String? =null
    val TAG = "NoteRVAdapter"


    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val noteTitleTV: TextView = itemView.findViewById(R.id.tvNoteTitle)
        val noteTextTV: TextView = itemView.findViewById(R.id.tvNoteDesc)
        val noteCard : View = itemView.findViewById(R.id.noteCard)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var title = allNotes[position].title
        var desc = allNotes[position].description
        val cm = Common()
        if (title?.length!! >20 ){
            title = title.substring(0,15)+"..."
        }
         if ( desc?.length !!> 250){
             desc= desc.substring(0,250) + "..."
         }

        holder.noteTitleTV.text = title
        holder.noteTextTV.text = desc
        Log.d(TAG, "onBindViewHolder: $searchString")
        searchString?.let { cm.setHighLightedText(holder.noteTextTV, it) }

        var colorID= R.color.white
        var textColorID =R.color.black



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



interface  NoteClickInterface{
    fun onNoteClick(note: Note)
}