package com.neuralbit.letsnote

import androidx.lifecycle.LiveData

class TagRepo(private val tagDao : NotesDao) {
    val allTags : LiveData<List<Tag>> = tagDao.getTags()

    suspend fun insert(tag : Tag) {
        tagDao.insertTag(tag)
    }

    suspend fun delete(tag : Tag) {
        tagDao.deleteTag(tag)
    }
}