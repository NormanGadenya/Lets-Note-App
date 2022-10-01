package com.neuralbit.letsnote.room.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.room.daos.TagDao
import com.neuralbit.letsnote.room.entities.Tag

class TagRoomRepo(private val tagDao : TagDao) {
    val allTags : LiveData<List<Tag>> = tagDao.getTags()

    suspend fun insert(tag : Tag) {
        tagDao.insertTag(tag)
    }

    suspend fun delete(tag : Tag) {
        tagDao.deleteTag(tag)
    }
}