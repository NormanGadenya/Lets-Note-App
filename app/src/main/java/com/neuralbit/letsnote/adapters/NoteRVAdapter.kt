package com.neuralbit.letsnote.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.LabelNotesViewModel
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.Note
import com.neuralbit.letsnote.entities.Reminder
import com.neuralbit.letsnote.entities.Tag
import com.neuralbit.letsnote.relationships.TagsWithNote
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.launch

class NoteRVAdapter (
    val context: Context,
    val noteClickInterface :NoteClickInterface,


    ): RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(){
    var viewModel : AllNotesViewModel ? = null
    var labelViewModel : LabelNotesViewModel? = null
    var lifecycleScope : LifecycleCoroutineScope? = null
    var lifecycleOwner: LifecycleOwner ? = null
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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

        if (title.isEmpty()) {
            holder.noteTitleTV.visibility = GONE
        } else {
            holder.noteTitleTV.text = title
            holder.noteTitleTV.visibility = VISIBLE

        }
        lifecycleScope?.launch {
            if (viewModel!=null){
                val tagList = viewModel?.getTagsWithNote(noteID)?.last()

                if (tagList?.tags?.isNotEmpty()!!) {
                    holder.tagsTV.visibility = VISIBLE

                }
                var tagStr : String? = null
                for (t in tagList.tags) {
                    tagStr = "#" + t.tagTitle + " "
                }
                if (tagStr!=null){
                    holder.tagsTV.text = tagStr
                    holder.tagsTV.visibility = VISIBLE

                }else{
                    holder.tagsTV.visibility = GONE

                }

            }

        }

        lifecycleOwner?.let {
            viewModel?.getReminder(noteID)?.observe(it) { r ->
                if (r != null) {
                    holder.reminderIcon.visibility = VISIBLE
                    holder.reminderTV.visibility = VISIBLE
                    holder.reminderTV.text = context.resources.getString(
                        R.string.reminder,
                        cm.convertLongToTime(r.dateTime)[0],
                        cm.convertLongToTime(r.dateTime)[1]
                    )

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

                        val noteCardColor = context.getColor(cm.getLabelColor(it.labelID))
                        holder.noteCard.setBackgroundColor(noteCardColor)
                        holder.noteTitleTV.setTextColor(context.getColor(cm.getFontColor(noteCardColor)))
                        holder.noteTextTV.setTextColor(context.getColor(cm.getFontColor(noteCardColor)))
                        holder.reminderTV.setTextColor(context.getColor(cm.getFontColor(noteCardColor)))
                    }

                }
            } else if(labelViewModel!=null) {
                labelViewModel?.getNoteLabel(noteID)?.observe(owner) {
                    val noteCardColor = context.getColor(cm.getLabelColor(it.labelID))
                    holder.noteCard.setBackgroundColor(noteCardColor)
                    holder.noteTitleTV.setTextColor(context.getColor(cm.getFontColor(noteCardColor)))
                    holder.noteTextTV.setTextColor(context.getColor(cm.getFontColor(noteCardColor)))
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


    fun updateReminder(r: Reminder){
        reminder = r
    }

}



interface  NoteClickInterface{
    fun onNoteClick(note: Note)
}