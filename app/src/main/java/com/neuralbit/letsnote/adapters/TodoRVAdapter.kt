package com.neuralbit.letsnote.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.TodoItem


class TodoRVAdapter(
    val context: Context,
    private val todoItemInterface: TodoItemInterface,


    ) : RecyclerView.Adapter<TodoRVAdapter.ViewHolder>() {

    lateinit var itemView: View
    private var allTodoItems = ArrayList<TodoItem>()
    val TAG = "TodoRVAdapter"





    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val todoItemDescET: EditText = itemView.findViewById(R.id.todoItemDescET)
        val checkBox : CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val deleteButton : ImageButton = itemView.findViewById(R.id.deleteItemBtn)
        val dragIndicator : ImageButton = itemView.findViewById(R.id.dragTodo)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        itemView = LayoutInflater.from(parent.context).inflate(R.layout.todo_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoItem = allTodoItems[position]
        holder.checkBox.isChecked = todoItem.checked
        holder.todoItemDescET.setText(todoItem.item)
        holder.checkBox.setOnCheckedChangeListener { checkBox, p1 ->
            todoItem.checked = p1
            todoItemInterface.onItemCheckChanged(holder.adapterPosition, todoItem)
        }
        holder.todoItemDescET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                todoItem.item = p0.toString()

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })



        holder.todoItemDescET.setOnKeyListener { _, key, v ->
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

