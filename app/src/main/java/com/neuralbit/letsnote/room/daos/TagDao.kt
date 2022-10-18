package com.neuralbit.letsnote.room.daos

import androidx.room.*
import com.neuralbit.letsnote.room.entities.Tag

@Dao
interface TagDao {

    @Insert(onConflict= OnConflictStrategy.REPLACE )
    suspend fun insertTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Transaction
    @Query("select * from Tag")
    suspend fun getTags(): List<Tag>

    @Query("DELETE FROM Tag")
    suspend fun deleteAllTags()
}