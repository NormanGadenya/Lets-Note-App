package com.neuralbit.letsnote.utilities;

import com.neuralbit.letsnote.entities.TodoItem;

import java.util.List;

public interface OnTodoListChangedListener {
    void onTodoListChanged(List<TodoItem> todoItems);
}