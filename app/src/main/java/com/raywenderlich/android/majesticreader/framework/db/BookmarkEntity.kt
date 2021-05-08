package com.raywenderlich.android.majesticreader.framework.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmark")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "documentUri") val documentUri: String,
    @ColumnInfo(name = "page") val page: Int
)