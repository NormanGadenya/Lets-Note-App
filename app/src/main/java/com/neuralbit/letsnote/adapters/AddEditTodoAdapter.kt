package com.neuralbit.letsnote.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.AddEditNoteActivity
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.TodoItem

class AddEditTodoAdapter(
    val context: Context,
    private val itemUpdate: ItemUpdate,
    private val parentClass: String
    

    ):RecyclerView.Adapter<AddEditTodoAdapter.ViewHolder>(){
    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView){
        val todoItemDescTV: EditText= itemView.findViewById(R.id.todoItemDescTV)
        val todoItemDescNoteRV: TextView= itemView.findViewById(R.id.todoItemDescNoteRV)
        val todoItemCheckBox: CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val todoDeleteBtn: ImageButton = itemView.findViewById(R.id.deleteItemBtn)
        val todoContainer: View = itemView.findViewById(R.id.container)
    }
    private var todoList = ArrayList<TodoItem>()
    var noteID: Long= 0
    val TAG = " ADDEDITTODO"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "onCreateViewHolder: $parentClass")
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.todo_rv_item,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoItem = todoList[position]

        holder.todoItemCheckBox.isChecked = todoItem.itemChecked
        if (parentClass=="NoteRVAdapter"){

            holder.todoItemDescNoteRV.visibility = VISIBLE
            holder.todoItemDescTV.visibility = GONE
            holder.todoDeleteBtn.visibility = GONE
            holder.todoItemCheckBox.width = 10
            holder.todoItemCheckBox.isEnabled = false
            holder.todoItemDescNoteRV
            holder.todoItemCheckBox.height = 10
            holder.todoItemDescNoteRV.text = todoItem.itemDesc
            holder.todoContainer.setOnClickListener {
                val intent = Intent( context, AddEditNoteActivity::class.java)
                intent.putExtra("noteType","Edit")
                intent.putExtra("noteID",noteID)

                context.startActivity(intent)
            }


        }
        holder.todoItemDescTV.setText(todoItem.itemDesc)

        holder.todoDeleteBtn.setOnClickListener {
            itemUpdate.onItemDelete(position,todoItem)

//            notifyItemRemoved(position)
        }
        //todo fix item checkbox bug

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
//        holder.todoItemDescTV.setOnEditorActionListener() { _, i, keyEvent ->
//            if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER){
//
//                itemUpdate.onEnterKeyPressed(position,todoItem)
//            }
//            holder.todoItemDescTV.isActivated = false
//
//            true
//        }
        holder.todoItemDescTV.setOnKeyListener { _, key, _ ->
            if (key == KeyEvent.KEYCODE_ENTER){
                itemUpdate.onEnterKeyPressed(position,todoItem)
            }


            false
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
    fun onEnterKeyPressed(position: Int,todoItem: TodoItem)
}