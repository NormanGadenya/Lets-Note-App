package com.neuralbit.letsnote

import android.content.Context

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Note::class,
        Tag::class,
        ArchivedNote::class,
        PinnedNote::class,
        NoteTagCrossRef::class
               ],version =3 , exportSchema = false)
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
                    "note_database17"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}