package com.neuralbit.letsnote.adapters

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.TodoItem

class AddEditTodoAdapter(
    val context: Context,
    val itemUpdate: ItemUpdate

    ):RecyclerView.Adapter<AddEditTodoAdapter.ViewHolder>(){
    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        val todoItemDescTV: EditText= itemView.findViewById(R.id.todoItemDescTV)
        val todoItemCheckBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val todoDeleteBtn: ImageButton = itemView.findViewById(R.id.deleteItemBtn)
    }
    private var todoList = ArrayList<TodoItem>()
    val TAG = " ADDEDITTODO"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.todo_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoItem = todoList[position]
        holder.todoItemDescTV.setText(todoItem.itemDesc)
        holder.todoItemCheckBox.isChecked = todoItem.itemChecked
        holder.todoDeleteBtn.setOnClickListener {
            itemUpdate.onItemDelete(position,todoItem)

//            notifyItemRemoved(position)
        }
        holder.todoItemCheckBox.setOnCheckedChangeListener { _, b ->
            todoItem.itemChecked = b


            itemUpdate.onItemCheckChanged(position,todoItem)
        }
        holder.todoItemDescTV.setOnFocusChangeListener { view, b ->
            if (!b){
                val text = holder.todoItemDescTV.text.toString()
                if (text.isNotEmpty()){
                    todoItem.itemDesc = holder.todoItemDescTV.text.toString()
                    itemUpdate.onItemDescChanged(position,todoItem)
                }else{ itemUpdate.onItemDelete(position,todoItem) }

            }

        }


    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    fun getTodoItems(newList: List<TodoItem>){
        todoList.clear()
        todoList.addAll(newList)
        notifyDataSetChanged()
    }
    fun updateTodoItem(newList: List<TodoItem>,position: Int){
        todoList.clear()
        todoList.addAll(newList)
        notifyItemChanged(position)
    }
}

interface ItemUpdate {
    fun onItemDelete(position : Int,todoItem: TodoItem)
    fun onItemCheckChanged(position: Int,todoItem: TodoItem)
    fun onItemDescChanged(position: Int, todoItem: TodoItem)
}