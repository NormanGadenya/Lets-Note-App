package com.neuralbit.letsnote.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.TodoItem

class AddEditTodoAdapter(
    val context: Context

    ):RecyclerView.Adapter<AddEditTodoAdapter.ViewHolder>(){
    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        val todoItemIDTV: TextView = itemView.findViewById(R.id.todoItemIDTV)
        val todoItemDescTV: EditText= itemView.findViewById(R.id.todoItemDescTV)
        val todoItemCheckBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val todoDeleteBtn: CheckBox = itemView.findViewById(R.id.deleteItemBtn)
    }
    private var todoList = ArrayList<TodoItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.todo_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoItem = todoList[position]
        holder.todoItemIDTV.text = todoItem.itemID.toString()
        holder.todoItemDescTV.setText(todoItem.itemDesc)
        holder.todoItemCheckBox.isChecked = todoItem.itemChecked
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    fun getTodoItems(todoList: ArrayList<TodoItem>){
        this.todoList = todoList
        notifyDataSetChanged()
    }
}