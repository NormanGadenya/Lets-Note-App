package com.neuralbit.letsnote.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.neuralbit.letsnote.daos.NotesDao
import com.neuralbit.letsnote.room.daos.LabelDao
import com.neuralbit.letsnote.room.daos.NoteTagDao
import com.neuralbit.letsnote.room.daos.ReminderDao
import com.neuralbit.letsnote.room.daos.TagDao
import com.neuralbit.letsnote.room.entities.*
import com.neuralbit.letsnote.room.relationships.NoteTagCrossRef

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
                    "letsNoteDB"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}