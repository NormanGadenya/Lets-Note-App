package com.neuralbit.letsnote.firebaseEntities

data class TodoItem (
    var item : String = "",
    var checked : Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = item.hashCode()
        result = 31 * result + checked.hashCode()
        return result
    }
}
