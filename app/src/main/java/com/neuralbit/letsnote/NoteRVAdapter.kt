package com.neuralbit.letsnote

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteRVAdapter (
    val context: Context,
    val noteClickInterface :NoteClickInterface,


    ): RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(){

    private val allNotes = ArrayList<Note>()
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
        var noteColor : String? = allNotes[position].tagColor
        if (title.length >20 ){
            title = title.substring(0,15)+"..."
        }
         if ( desc.length > 250){
             desc= desc.substring(0,250) + "..."
         }

        holder.noteTitleTV.text = title
        holder.noteTextTV.text = desc
        var colorID= R.color.white
        var textColorID =R.color.black
        if (noteColor !=null){
            when(noteColor) {
                "White" -> {
                    colorID = R.color.white
                }
                "English_violet" -> {
                    colorID = R.color.English_violet
                    textColorID = Color.WHITE
                }
                "Wild_orchid" -> { colorID = R.color.Wild_orchid }
                "Celadon" -> { colorID = R.color.Celadon }
                "Honeydew" -> { colorID = R.color.Honeydew }
                "Apricot" -> { colorID = R.color.Apricot }
            }
            Log.d("TAG", "onBindViewHolder: $noteColor")
            holder.noteCard.setBackgroundColor(context.resources.getColor(colorID))
            holder.noteTitleTV.setTextColor(textColorID)
            holder.noteTextTV.setTextColor(textColorID)
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



interface  NoteClickInterface{
    fun onNoteClick(note:Note)
}