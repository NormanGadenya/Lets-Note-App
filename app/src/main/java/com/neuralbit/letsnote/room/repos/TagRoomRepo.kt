package com.neuralbit.letsnote.room.repos

import com.neuralbit.letsnote.room.daos.TagDao
import com.neuralbit.letsnote.room.entities.Tag

class TagRoomRepo(private val tagDao : TagDao) {

    suspend fun insert(tag : Tag) {
        tagDao.insertTag(tag)
    }

    suspend fun delete(tag : Tag) {
        tagDao.deleteTag(tag)
    }


    suspend fun getAllTags() : List<Tag> {
        return tagDao.getTags()
    }
}