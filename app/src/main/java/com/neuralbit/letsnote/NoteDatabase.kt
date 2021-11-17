package com.neuralbit.letsnote

import android.content.Context
import android.nfc.Tag
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Note::class,ArchivedNote::class),version =3 , exportSchema = false)
abstract class NoteDatabase : RoomDatabase(){
    abstract fun getNotesDao() : NotesDao

    companion object {
        @Volatile
        private var INSTANCE : NoteDatabase? = null
        fun getDatabase (context: Context): NoteDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database3"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}