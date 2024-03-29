package com.neuralbit.letsnote.ui.adapters

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.firebase.entities.NoteFire
import com.neuralbit.letsnote.receivers.AlertReceiver
import com.neuralbit.letsnote.ui.allNotes.AllNotesViewModel
import com.neuralbit.letsnote.utilities.Common
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.floor


class NoteRVAdapter (
    val context: Context,
    private val noteFireClick :NoteFireClick,



    ): RecyclerView.Adapter<NoteRVAdapter.ViewHolder>(){
    var viewModel : AllNotesViewModel ? = null
    var lifecycleScope : LifecycleCoroutineScope? = null
    var lifecycleOwner: LifecycleOwner ? = null
    lateinit var itemView: View
    private var allNotesFire : List<NoteFire> = ArrayList<NoteFire>()
    var deleteFrag = false
    var searchString: String? =null
    var multipleActivated = false
    val TAG = "NoteRVAdapter"
    var fontStyle : String? = null


    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val noteTitleTV: TextView = itemView.findViewById(R.id.tvNoteTitle)
        val noteTextTV: TextView = itemView.findViewById(R.id.tvNoteDesc)
        val noteCard : View = itemView.findViewById(R.id.noteCard)
        val tagsTV : TextView = itemView.findViewById(R.id.noteTagsTV)
        val reminderTV : TextView = itemView.findViewById(R.id.reminderTV)
        val reminderIcon: View = itemView.findViewById(R.id.reminderIcon)
        val daysLeft : TextView = itemView.findViewById(R.id.timeLeftDeleteTV)
        val lockIcon : ImageView = itemView.findViewById(R.id.noteLockedIcon)
        val todoIcon : ImageView = itemView.findViewById(R.id.todoIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = allNotesFire[position]
        var title = note.title
        val cm = Common()
        if (fontStyle != null){
            try {
                val typeface: Typeface? = when (fontStyle) {
                    cm.ARCHITECTS_DAUGHTER -> {
                        ResourcesCompat.getFont(context, R.font.architects_daughter)
                    }
                    cm.ABREEZE -> {
                        ResourcesCompat.getFont(context, R.font.abeezee)
                    }
                    cm.ADAMINA -> {
                        ResourcesCompat.getFont(context, R.font.adamina)
                    }
                    cm.BELLEZA -> {
                        ResourcesCompat.getFont(context, R.font.belleza)
                    }
                    cm.JOTI_ONE -> {
                        ResourcesCompat.getFont(context, R.font.joti_one)
                    }
                    cm.NOVA_FLAT -> {
                        ResourcesCompat.getFont(context, R.font.nova_flat)
                    }
                    else -> {
                        ResourcesCompat.getFont(context, R.font.roboto)
                    }
                }
                holder.noteTextTV.typeface = typeface
                holder.noteTitleTV.typeface = typeface
                holder.daysLeft.typeface = typeface
                holder.reminderTV.typeface = typeface
                holder.tagsTV.typeface = typeface
            }catch (_: Exception){

            }

        }
        val settingsPref = context.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val fontMultiplier = settingsPref.getInt("fontMultiplier",2)
        GlobalScope.launch {
            setFontSize(holder, fontMultiplier)
        }

        if (!note.protected){
            var desc = note.description
            if (desc.length > 250) {
                desc = desc.substring(0, 250) + "..."
            }
            val todoItems = note.todoItems
            GlobalScope.launch {
                for (todoItem in todoItems) {
                    desc += if (todoItem.checked){
                        "\n + ${todoItem.item} "
                    }else{

                        "\n - ${todoItem.item} "
                    }
                }
                withContext(Dispatchers.Main){
                    if (desc.isEmpty()) {
                        holder.noteTextTV.visibility = GONE
                    } else {
                        holder.noteTextTV.text = desc
                        holder.noteTextTV.visibility = VISIBLE
                    }
                }
            }



            if (todoItems.isEmpty()){
                holder.todoIcon.visibility = GONE
            }else{
                holder.todoIcon.visibility = VISIBLE

            }

        }else{
            holder.noteTextTV.text = "**protected**"
            val todoItems = note.todoItems
            if (todoItems.isEmpty()){
                holder.todoIcon.visibility = GONE
            }else{
                holder.todoIcon.visibility = VISIBLE

            }

        }
        if (title.length > 20) {
            title = title.substring(0, 15) + "..."
        }



        val c = Calendar.getInstance()

        lifecycleOwner?.let {
            viewModel?.itemSelectEnabled?.observe(it){ i ->

                if (!i){
                    note.selected = false

                    viewModel?.selectedNotes?.clear()
                    if (note.label > 0){
                        holder.noteCard.setBackgroundColor(note.label)
                    }else{
                        holder.noteCard.setBackgroundColor(context.resources.getColor(R.color.def_Card_Color,null))
                    }
                }
            }
        }

        if (deleteFrag){
            holder.daysLeft.visibility = VISIBLE
            val deletedTime = note.deletedDate
            val timeLeftMS = deletedTime +  6.048e+8 - System.currentTimeMillis()
            val daysLeft = floor(timeLeftMS * 1.1574E-8)
            if (daysLeft >1 || daysLeft== (0).toDouble()){
                holder.daysLeft.text = "${daysLeft.toInt()} days left "
            }else if (daysLeft == (1).toDouble()){
                holder.daysLeft.text = "${deletedTime} day left"
            }


        }else{
            holder.daysLeft.visibility = GONE
        }

        if (note.protected){
            holder.lockIcon.visibility = VISIBLE
        }else{
            holder.lockIcon.visibility = GONE
        }

        if (title.isEmpty()) {
            holder.noteTitleTV.visibility = GONE
        } else {
            holder.noteTitleTV.text = title
            holder.noteTitleTV.visibility = VISIBLE

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
        val tagBackgroundDrawable: Drawable = context.getResources().getDrawable(R.drawable.tag_background)
        var tagBackgroundDrawableWrapped = DrawableCompat.wrap(tagBackgroundDrawable)
        tagBackgroundDrawableWrapped = tagBackgroundDrawableWrapped.mutate()
        if (note.label > 0) {
            holder.noteCard.setBackgroundColor(note.label)
            holder.noteTextTV.setTextColor(cm.darkenColor(note.label,0.8f))
            holder.noteTitleTV.setTextColor(cm.darkenColor(note.label, 0.8f))
            holder.reminderTV.setTextColor(cm.darkenColor(note.label, 0.8f))
            //tags
            DrawableCompat.setTint(tagBackgroundDrawableWrapped, cm.darkenColor(note.label, 0.8f))
            holder.tagsTV.background = tagBackgroundDrawableWrapped
            holder.tagsTV.setTextColor(cm.lightenColor(note.label, 0.8f))

            holder.reminderIcon.backgroundTintList = ColorStateList.valueOf(cm.darkenColor(note.label, 0.8f))
            holder.lockIcon.backgroundTintList = ColorStateList.valueOf(cm.darkenColor(note.label, 0.8f))
            holder.todoIcon.backgroundTintList = ColorStateList.valueOf(cm.darkenColor(note.label, 0.8f))

        }else{
            holder.noteCard.setBackgroundColor(context.resources.getColor(R.color.def_Card_Color,null))
            holder.noteTextTV.setTextColor(cm.darkenColor(R.color.def_Card_Color,0.8f))
            holder.noteTitleTV.setTextColor(cm.darkenColor(R.color.def_Card_Color, 0.8f))
            DrawableCompat.setTint(tagBackgroundDrawableWrapped, cm.darkenColor(R.color.def_Card_Color, 0.8f))
            holder.tagsTV.background = tagBackgroundDrawableWrapped
            holder.tagsTV.setTextColor(cm.lightenColor(R.color.def_Card_Color, 0.8f))

            holder.reminderIcon.backgroundTintList = ColorStateList.valueOf(cm.darkenColor(R.color.def_Card_Color, 0.8f))
            holder.lockIcon.backgroundTintList = ColorStateList.valueOf(cm.darkenColor(R.color.def_Card_Color, 0.8f))
            holder.todoIcon.backgroundTintList = ColorStateList.valueOf(cm.darkenColor(R.color.def_Card_Color, 0.8f))

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
            setHighlightFontSize(holder.noteTextTV, it)
            setHighlightFontSize(holder.noteTitleTV, it)

        }
        lifecycleOwner?.let { owner ->
            viewModel?.itemSelectEnabled?.observe(owner){
            multipleActivated = it
        }}

        holder.itemView.setOnClickListener {
            if (multipleActivated){
                if (!note.selected){
                    holder.noteCard.setBackgroundColor(context.resources.getColor(R.color.sel_card_color,null))

                    note.selected = true
                    note.itemPosition = holder.adapterPosition
                    viewModel?.selectedNotes?.add(note)

                }else{
                    note.selected = false
                    if (note.label > 0){
                        holder.noteCard.setBackgroundColor(note.label)

                    }else{
                        holder.noteCard.setBackgroundColor(context.resources.getColor(R.color.def_Card_Color,null))
                    }

                    viewModel?.selectedNotes?.remove(note)

                }
            }

            noteFireClick.onNoteFireClick(note,multipleActivated)
            if (multipleActivated && viewModel?.selectedNotes?.isEmpty() == true){
                multipleActivated = false
                viewModel?.itemSelectEnabled?.value = false
            }
        }

        holder.itemView.setOnLongClickListener {

            if (!note.selected && !multipleActivated){
                multipleActivated = true
                note.selected = true
                note.itemPosition = holder.adapterPosition
                holder.noteCard.setBackgroundColor(context.resources.getColor(R.color.sel_card_color,null))

                viewModel?.selectedNotes?.add(note)
                noteFireClick.onNoteFireLongClick(note)
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }
    }

    private suspend fun setFontSize(
        holder: ViewHolder,
        fontMultiplier: Int
    ) {
        withContext(Dispatchers.Main){

            holder.noteTextTV.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                16f + ((fontMultiplier - 2) * 4).toFloat()
            )
            holder.noteTitleTV.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                24f + ((fontMultiplier - 2) * 4).toFloat()
            )
            holder.reminderTV.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                12f + ((fontMultiplier - 2)).toFloat()
            )
            holder.tagsTV.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                12f + ((fontMultiplier - 2)).toFloat()
            )
        }
    }

    override fun getItemCount(): Int {
        return allNotesFire.size
    }

    fun setHighlightFontSize(tv: TextView, textToHighlight: String){
        GlobalScope.launch {

            val tvt = tv.text.toString()
            var ofe = tvt.indexOf(textToHighlight, 0)
            val wordToSpan: Spannable = SpannableString(tv.text)
            var ofs = 0
            while (ofs < tvt.length && ofe != -1) {
                ofe = tvt.indexOf(textToHighlight, ofs)
                if (ofe == -1) break else {
                    wordToSpan.setSpan(
                        RelativeSizeSpan(1.5f),
                        ofe,
                        ofe + textToHighlight.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    withContext(Dispatchers.Main){
                        tv.setText(wordToSpan, TextView.BufferType.SPANNABLE)
                    }
                }
                ofs = ofe + 1
            }
        }



    }

    fun updateListFire( newList: List<NoteFire>){
        allNotesFire = newList

        notifyDataSetChanged()
    }

    private fun cancelAlarm(reminder : Int){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, reminder, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }



}


interface NoteFireClick{
    fun onNoteFireClick(note : NoteFire, activated : Boolean)
    fun onNoteFireLongClick(note: NoteFire)
}