package com.neuralbit.letsnote.ui.addEditNote

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.firebase.entities.TodoItem


class TodoRVAdapter(
    val context: Context,
    private val todoItemInterface: TodoItemInterface
    ) : RecyclerView.Adapter<TodoRVAdapter.ViewHolder>() {

    lateinit var itemView: View
    private var allTodoItems = ArrayList<TodoItem>()
    val TAG = "TodoRVAdapter"
    var fontStyle : String? = null
    var fontMultiplier : Int = 2
    var viewModel : NoteViewModel? = null
    var lifecycleOwner: LifecycleOwner? = null

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val todoItemDescET: EditText = itemView.findViewById(R.id.todoItemDescET)
        val checkBox : CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val deleteButton : ImageButton = itemView.findViewById(R.id.deleteItemBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        itemView = LayoutInflater.from(parent.context).inflate(R.layout.todo_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoItem = allTodoItems[position]
        holder.checkBox.isChecked = todoItem.checked

        lifecycleOwner?.let {
            viewModel?.deletedNote?.observe(it){ d ->
                if (!d){
                    viewModel?.archived?.observe(it){ a ->
                        if (a){
                            holder.todoItemDescET.isEnabled = false
                            holder.checkBox.isEnabled = false
                            holder.deleteButton.isEnabled = false
                        }else{
                            holder.todoItemDescET.isEnabled = true
                            holder.checkBox.isEnabled = true
                            holder.deleteButton.isEnabled = true
                        }
                    }
                }else{
                    holder.todoItemDescET.isEnabled = true
                    holder.checkBox.isEnabled = true
                    holder.deleteButton.isEnabled = true
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val typeface: Typeface? = when (fontStyle) {
                "Architects daughter" -> {
                    ResourcesCompat.getFont(context, R.font.architects_daughter)
                }
                "Abreeze" -> {
                    ResourcesCompat.getFont(context, R.font.abeezee)
                }
                "Adamina" -> {
                    ResourcesCompat.getFont(context, R.font.adamina)
                }
                else -> {
                    ResourcesCompat.getFont(context, R.font.roboto)
                }
            }
            holder.todoItemDescET.typeface = typeface

        }
        holder.todoItemDescET.setTextSize(TypedValue.COMPLEX_UNIT_SP,18f+ ((fontMultiplier-2)*2).toFloat())

        if (todoItem.item.isNotEmpty() && todoItem.checked){
            val spannableString = SpannableString(todoItem.item)
            val strikethroughSpan = StrikethroughSpan()
            spannableString.setSpan(strikethroughSpan, 0, spannableString.length , SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.todoItemDescET.setText(spannableString)
            holder.todoItemDescET.setTextColor(context.resources.getColor(R.color.sel_card_color))
        }else{
            holder.todoItemDescET.setText(todoItem.item)
        }
        holder.checkBox.setOnCheckedChangeListener { _, p1 ->
            todoItem.checked = p1
            if (todoItem.checked){
                val spannableString = SpannableString(todoItem.item)
                val strikethroughSpan = StrikethroughSpan()
                spannableString.setSpan(strikethroughSpan, 0, spannableString.length , SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.todoItemDescET.setText(spannableString)
                holder.todoItemDescET.setTextColor(context.resources.getColor(R.color.sel_card_color))
            }else{
                holder.todoItemDescET.setText(todoItem.item)
                holder.todoItemDescET.setTextColor(context.resources.getColor(R.color.black))

            }
            todoItemInterface.onItemCheckChanged(holder.adapterPosition, todoItem)
        }
        holder.todoItemDescET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                todoItem.item = p0.toString()

            }

            override fun afterTextChanged(p0: Editable?) {
                todoItem.item = p0.toString()
                todoItemInterface.onItemDescChanged(holder.adapterPosition, todoItem)

            }

        })



        holder.todoItemDescET.setOnKeyListener { _, key, _ ->
            if (key == KeyEvent.KEYCODE_ENTER){
                holder.todoItemDescET.clearFocus()
                todoItemInterface.onEnterKeyPressed(holder.adapterPosition,todoItem)
                return@setOnKeyListener true
            }

            false
        }
        holder.deleteButton.setOnClickListener {
            todoItemInterface.onItemDelete(holder.adapterPosition,todoItem)
        }

    }


    fun updateTodoItems(newList: List<TodoItem>){
        allTodoItems = ArrayList(newList)
    }


    override fun getItemCount(): Int {
        return allTodoItems.size
    }


}
interface TodoItemInterface{
    fun onItemDelete(position : Int,todoItem: TodoItem)
    fun onItemCheckChanged(position: Int,todoItem: TodoItem)
    fun onItemDescChanged(position: Int, todoItem: TodoItem)
    fun onEnterKeyPressed(position: Int,todoItem: TodoItem)
}

