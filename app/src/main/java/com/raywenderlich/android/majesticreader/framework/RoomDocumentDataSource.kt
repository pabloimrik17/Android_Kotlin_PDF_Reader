package com.raywenderlich.android.majesticreader.framework

import android.content.Context
import com.raywenderlich.android.majesticreader.data.DocumentDataSource
import com.raywenderlich.android.majesticreader.domain.Document
import com.raywenderlich.android.majesticreader.framework.db.DocumentEntity
import com.raywenderlich.android.majesticreader.framework.db.MajesticReaderDatabase

class RoomDocumentDataSource(val context: Context): DocumentDataSource {
    private val documentDao =MajesticReaderDatabase.getInstance(context).documentDao()

    override suspend fun add(document: Document) {
        val details = FileUtil.getDocumentDetails(context, document.url)

        documentDao.addDocument(
            DocumentEntity(document.url, details.name, details.size, details.thumbnail)
        )
    }

    override suspend fun readAll(): List<Document> {
        return documentDao.getDocuments().map {
            Document(
                it.uri,
                it.title,
                it.size,
                it.thumbnailUri
            )
        }
    }

    override suspend fun remove(document: Document) {
        documentDao.removeDocument(
            DocumentEntity(document.url, document.name, document.size, document.thumbnail)
        )
    }
}