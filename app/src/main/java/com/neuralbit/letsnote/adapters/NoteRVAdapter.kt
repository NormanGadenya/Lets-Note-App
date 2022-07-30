package com.neuralbit.letsnote.adapters

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.NoteFire
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.utilities.AlertReceiver
import com.neuralbit.letsnote.utilities.Common
import java.util.*


class NoteRVAdapter (
    val context: Context,
    private val noteFireClick :NoteFireClick,


    ): RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(){
    var viewModel : AllNotesViewModel ? = null
    var lifecycleScope : LifecycleCoroutineScope? = null
    var lifecycleOwner: LifecycleOwner ? = null
    lateinit var itemView: View
    private val allNotesFire = ArrayList<NoteFire>()
    var searchString: String? =null
    val TAG = "NoteRVAdapter"


    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val noteTitleTV: TextView = itemView.findViewById(R.id.tvNoteTitle)
        val noteTextTV: TextView = itemView.findViewById(R.id.tvNoteDesc)
        val noteCard : View = itemView.findViewById(R.id.noteCard)
        val tagsTV : TextView = itemView.findViewById(R.id.noteTagsTV)
        val reminderTV : TextView = itemView.findViewById(R.id.reminderTV)
        val reminderIcon: View = itemView.findViewById(R.id.reminderIcon)
        val todoRV : RecyclerView = itemView.findViewById(R.id.todoRV)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = allNotesFire[position]

        val layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        holder.todoRV.layoutManager = layoutManager
        var title = note.title
        var desc = note.description
        val cm = Common()
        if (title.length > 20) {
            title = title.substring(0, 15) + "..."
        }
        if (desc.length > 250) {
            desc = desc.substring(0, 250) + "..."
        }
        val todoItems = note.todoItems
        for (todoItem in todoItems) {
            desc += if (todoItem.checked){
                "\n + ${todoItem.item} "
            }else{

                "\n - ${todoItem.item} "
            }
        }
        val c = Calendar.getInstance()


        if (title.isEmpty()) {
            holder.noteTitleTV.visibility = GONE
        } else {
            holder.noteTitleTV.text = title
            holder.noteTitleTV.visibility = VISIBLE

        }
        if (desc.isEmpty()) {
            holder.noteTextTV.visibility = GONE
        } else {
            holder.noteTextTV.text = desc
            holder.noteTextTV.visibility = VISIBLE
        }
        var tagStr = ""
        for (tag in note.tags) {
            tagStr = "$tagStr $tag "
            if (tagStr.length > 20){
                break
            }
        }
        if (tagStr.isNotEmpty()){
            holder.tagsTV.text = tagStr
            holder.tagsTV.visibility = VISIBLE
        }else{
            holder.tagsTV.visibility = GONE
        }
        if (note.label > 0){
            holder.noteCard.setBackgroundColor(note.label)
        }

        val reminderDate = note.reminderDate

        if (reminderDate > 0) {

            if(c.timeInMillis > reminderDate){
                holder.reminderIcon.visibility = GONE
                holder.reminderTV.visibility = GONE
                cancelAlarm(reminderDate.toInt())
            }else{
                holder.reminderIcon.visibility = VISIBLE
                holder.reminderTV.visibility = VISIBLE
                holder.reminderTV.text = context.resources.getString(
                    R.string.reminder,
                    cm.convertLongToTime(reminderDate)[0],
                    cm.convertLongToTime(reminderDate)[1]
                )

            }


        } else {
            holder.reminderIcon.visibility = GONE
            holder.reminderTV.visibility = GONE
        }


        searchString?.let {
            cm.setHighLightedText(holder.noteTextTV, it)
            cm.setHighLightedText(holder.noteTitleTV, it)

        }

        holder.itemView.setOnClickListener {
            noteFireClick.onNoteFireClick(note)
        }
    }
    override fun getItemCount(): Int {
        return allNotesFire.size
    }


    fun updateListFire( newList: List<NoteFire>){
        allNotesFire.clear()
        allNotesFire.addAll(newList)
        notifyItemRangeChanged(0,allNotesFire.size)
    }

    private fun cancelAlarm(reminder : Int){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, reminder, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

}


interface NoteFireClick{
    fun onNoteFireClick(note : NoteFire)
}