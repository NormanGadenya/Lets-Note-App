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
import com.neuralbit.letsnote.LabelNotesViewModel
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.Reminder
import com.neuralbit.letsnote.entities.TodoItem
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.utilities.AlertReceiver
import com.neuralbit.letsnote.utilities.Common
import java.util.*

class NoteRVAdapter (
    val context: Context,
    private val noteClickInterface :NoteClickInterface,


    ): RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(),ItemUpdate{
    var viewModel : AllNotesViewModel ? = null
    var labelViewModel : LabelNotesViewModel? = null
    var lifecycleScope : LifecycleCoroutineScope? = null
    var lifecycleOwner: LifecycleOwner ? = null
    lateinit var itemView: View
    private val allNotes = ArrayList<Note>()
    private val tags : String? = null
    private var reminder : Reminder? = null
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
        val todoAdapter = AddEditTodoAdapter(context,this,"NoteRVAdapter")

        val layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        holder.todoRV.layoutManager = layoutManager
        holder.todoRV.adapter = todoAdapter

        var title = allNotes[position].title
        var desc = allNotes[position].description
        val cm = Common()
        val noteID = allNotes[position].noteID
        if (title?.length!! > 20) {
            title = title.substring(0, 15) + "..."
        }
        if (desc?.length!! > 250) {
            desc = desc.substring(0, 250) + "..."
        }
        val c = Calendar.getInstance()


        if (title.isEmpty()) {
            holder.noteTitleTV.visibility = GONE
        } else {
            holder.noteTitleTV.text = title
            holder.noteTitleTV.visibility = VISIBLE

        }


//        lifecycleScope?.launch {
//            if (viewModel!=null){
//                todoAdapter.noteID = noteID
//                viewModel!!.getTodoList(noteID).observe(lifecycleOwner!!){
//                    todoAdapter.getTodoItems(it)
//                }
////                val tagList = viewModel?.getTagsWithNote(noteID)?.last()
//                var tagList = viewModel?.getTagsWithNote(noteID).let { it?.last() }
//                if (tagList?.tags?.isNotEmpty()!!) {
//                    holder.tagsTV.visibility = VISIBLE
//
//                }
//                var tagStr : String? = null
//
//                for (t in tagList.tags) {
//                    tagStr = t.tagTitle + " "
//                }
//                if (tagStr!=null){
//                    holder.tagsTV.text = tagStr
//                    holder.tagsTV.visibility = VISIBLE
//
//                }else{
//                    holder.tagsTV.visibility = GONE
//
//                }
//
//            }
//
//        }

        lifecycleOwner?.let {
            viewModel?.getReminder(noteID)?.observe(it) { r ->
                if (r != null) {

                    if(c.timeInMillis > r.dateTime){
                        cancelAlarm(noteID.toInt())
                        viewModel?.deleteReminder(noteID)
                    }else{
                        holder.reminderIcon.visibility = VISIBLE
                        holder.reminderTV.visibility = VISIBLE
                        holder.reminderTV.text = context.resources.getString(
                            R.string.reminder,
                            cm.convertLongToTime(r.dateTime)[0],
                            cm.convertLongToTime(r.dateTime)[1]
                        )

                    }


                } else {
                    holder.reminderIcon.visibility = GONE
                    holder.reminderTV.visibility = GONE
                }
            }
        }

        lifecycleOwner?.let { owner ->
            if (viewModel != null) {
                viewModel?.getNoteLabel(noteID)?.observe(owner) {
                    if(it!=null){

                        val noteCardColor = it.labelID
                        holder.noteCard.setBackgroundColor(noteCardColor)

                    }

                }
            } else if(labelViewModel!=null) {
                labelViewModel?.getNoteLabel(noteID)?.observe(owner) {
                    val noteCardColor = context.getColor(it.labelID)
                    holder.noteCard.setBackgroundColor(noteCardColor)
                }

            }

            if (desc.isEmpty()) {
                holder.noteTextTV.visibility = GONE
            } else {
                holder.noteTextTV.text = desc
                holder.noteTextTV.visibility = VISIBLE
            }


            searchString?.let {
                cm.setHighLightedText(holder.noteTextTV, it)
                cm.setHighLightedText(holder.noteTitleTV, it)

            }





            holder.itemView.setOnClickListener {
                noteClickInterface.onNoteClick(allNotes.get(position))
            }
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
    fun undoDelete( swipedNote: Note,oldPosition : Int){
//        allNotes.clear()
        allNotes.add(oldPosition,swipedNote)
        notifyItemInserted(oldPosition)
    }

    fun restoreItemColor (){
        val c = ViewHolder(itemView)
        c.noteCard.setBackgroundColor(0)
    }
    fun updateReminder(r: Reminder){
        reminder = r
    }

    private fun cancelAlarm(noteID : Int){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, noteID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)
    }

    override fun onItemDelete(position: Int, todoItem: TodoItem) {
        TODO("Not yet implemented")
    }

    override fun onItemCheckChanged(position: Int, todoItem: TodoItem) {
        TODO("Not yet implemented")
    }

    override fun onItemDescChanged(position: Int, todoItem: TodoItem) {
        TODO("Not yet implemented")
    }

    override fun onEnterKeyPressed(position: Int, todoItem: TodoItem) {
        TODO("Not yet implemented")
    }


}




interface  NoteClickInterface{
    fun onNoteClick(note: Note)
}