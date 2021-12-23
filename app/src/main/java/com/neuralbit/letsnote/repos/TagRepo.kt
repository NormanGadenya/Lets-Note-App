package com.neuralbit.letsnote.repos

import androidx.lifecycle.LiveData
import com.neuralbit.letsnote.daos.TagDao
import com.neuralbit.letsnote.entities.Tag

class TagRepo(private val tagDao : TagDao) {
    val allTags : LiveData<List<Tag>> = tagDao.getTags()

    suspend fun insert(tag : Tag) {
        tagDao.insertTag(tag)
    }

    suspend fun delete(tag : Tag) {
        tagDao.deleteTag(tag)
    }
}