package com.neuralbit.letsnote.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.neuralbit.letsnote.room.daos.LabelDao
import com.neuralbit.letsnote.room.daos.NoteTagDao
import com.neuralbit.letsnote.room.daos.NotesDao
import com.neuralbit.letsnote.room.daos.TagDao
import com.neuralbit.letsnote.room.entities.Label
import com.neuralbit.letsnote.room.entities.Note
import com.neuralbit.letsnote.room.entities.Tag
import com.neuralbit.letsnote.room.entities.TodoItem
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef

@Database(
    entities = [
        Note::class,
        Tag::class,
        NoteTagCrossRef::class,
        TodoItem::class,
        Label::class
    ],version =8 , exportSchema = true)
abstract class NoteDatabase : RoomDatabase(){
    abstract fun getNotesDao() : NotesDao
    abstract fun getTagDao() : TagDao
    abstract fun getNoteTagDao() : NoteTagDao
    abstract fun getLabelDao() : LabelDao


    companion object {
        @Volatile
        private var INSTANCE : NoteDatabase? = null
        fun getDatabase (context: Context): NoteDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "letsNote4"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}