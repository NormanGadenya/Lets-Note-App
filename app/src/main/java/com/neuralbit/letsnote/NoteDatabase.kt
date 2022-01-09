package com.neuralbit.letsnote

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.neuralbit.letsnote.daos.*
import com.neuralbit.letsnote.entities.*

@Database(
    entities = [
        Note::class,
        Tag::class,
        ArchivedNote::class,
        PinnedNote::class,
        NoteTagCrossRef::class,
        Reminder::class,
        Label::class
               ],version =5 , exportSchema = true)
abstract class NoteDatabase : RoomDatabase(){
    abstract fun getNotesDao() : NotesDao
    abstract fun getTagDao() : TagDao
    abstract fun getNoteTagDao() : NoteTagDao
    abstract fun getReminderDao() : ReminderDao
    abstract fun getLabelDao() : LabelDao


    companion object {
        @Volatile
        private var INSTANCE : NoteDatabase? = null
        fun getDatabase (context: Context): NoteDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database34"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}