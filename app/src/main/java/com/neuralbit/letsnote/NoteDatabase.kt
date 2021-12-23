package com.neuralbit.letsnote

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.neuralbit.letsnote.daos.NoteTagDao
import com.neuralbit.letsnote.daos.NotesDao
import com.neuralbit.letsnote.daos.TagDao
import com.neuralbit.letsnote.entities.*

@Database(
    entities = [
        Note::class,
        Tag::class,
        ArchivedNote::class,
        PinnedNote::class,
        NoteTagCrossRef::class
               ],version =4 , exportSchema = true)
abstract class NoteDatabase : RoomDatabase(){
    abstract fun getNotesDao() : NotesDao
    abstract fun getTagDao() : TagDao
    abstract fun getNoteTagDao() : NoteTagDao


    companion object {
        @Volatile
        private var INSTANCE : NoteDatabase? = null
        fun getDatabase (context: Context): NoteDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database18"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}