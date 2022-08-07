package com.neuralbit.letsnote.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.neuralbit.letsnote.R
import com.neuralbit.letsnote.entities.TodoItem
import com.neuralbit.letsnote.utilities.ItemTouchHelperAdapter
import com.neuralbit.letsnote.utilities.ItemTouchHelperViewHolder
import com.neuralbit.letsnote.utilities.OnStartDragListener
import com.neuralbit.letsnote.utilities.OnTodoListChangedListener
import java.util.*


class TodoRVAdapter(
    val context: Context,
    private val todoItemInterface: TodoItemInterface,
    private val dragListener: OnStartDragListener?,
    private val listChangedListener: OnTodoListChangedListener

    ) : ItemTouchHelperAdapter , RecyclerView.Adapter<TodoRVAdapter.ViewHolder>() {

    lateinit var itemView: View
    private val allTodoItems = ArrayList<TodoItem>()
    val TAG = "TodoRVAdapter"





    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder{
        val todoItemDescET: EditText = itemView.findViewById(R.id.todoItemDescET)
        val checkBox : CheckBox = itemView.findViewById(R.id.todoCheckBox)
        val deleteButton : ImageButton = itemView.findViewById(R.id.deleteItemBtn)
        val dragIndicator : ImageButton = itemView.findViewById(R.id.dragTodo)

        override fun onItemSelected() {

        }

        override fun onItemClear() {

        }
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

//        holder.dragIndicator.setOnTouchListener({ v, event ->
//            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
//                dragLlistener?.onStartDrag(holder)
//            }
//            false
//        })
        holder.dragIndicator.setOnLongClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

            dragListener?.onStartDrag(holder)
            return@setOnLongClickListener true
        }

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

    override fun getItemCount(): Int {
        return allTodoItems.size
    }


    fun updateTodoItems( newList: List<TodoItem>){
        allTodoItems.clear()
        allTodoItems.addAll(newList)
        notifyItemRangeChanged(0,newList.size)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(allTodoItems, fromPosition, toPosition)
        listChangedListener.onTodoListChanged(allTodoItems)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
    }


}
interface TodoItemInterface{
    fun onItemDelete(position : Int,todoItem: TodoItem)
    fun onItemCheckChanged(position: Int,todoItem: TodoItem)
    fun onItemDescChanged(position: Int, todoItem: TodoItem)
    fun onEnterKeyPressed(position: Int,todoItem: TodoItem)
}

