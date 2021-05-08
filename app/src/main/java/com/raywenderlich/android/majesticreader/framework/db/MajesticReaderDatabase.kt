package com.raywenderlich.android.majesticreader.framework.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BookmarkEntity::class, DocumentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MajesticReaderDatabase : RoomDatabase() {

  companion object {

    private const val DATABASE_NAME = "reader.db"

    private var instance: MajesticReaderDatabase? = null

    private fun create(context: Context): MajesticReaderDatabase =
        Room.databaseBuilder(context, MajesticReaderDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()


    fun getInstance(context: Context): MajesticReaderDatabase =
        (instance ?: create(context)).also { instance = it }
  }

  abstract fun bookmarkDao(): BookmarkDao

  abstract fun documentDao(): DocumentDao

}