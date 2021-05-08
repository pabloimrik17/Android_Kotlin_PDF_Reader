package com.raywenderlich.android.majesticreader.data

import com.raywenderlich.android.majesticreader.domain.Bookmark
import com.raywenderlich.android.majesticreader.domain.Document

interface BookmarkDataSource {
    suspend fun add(document: Document, bookmark: Bookmark)

    suspend fun read(document: Document): List<Bookmark>

    suspend fun remove(document: Document, bookmark: Bookmark)
}