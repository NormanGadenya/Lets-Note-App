package com.neuralbit.letsnote

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TagDao {

    @Insert(onConflict= OnConflictStrategy.IGNORE )
    suspend fun insertTag(tag:Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("select * from TagTable")
    fun getTags(): LiveData<List<Tag>>
}