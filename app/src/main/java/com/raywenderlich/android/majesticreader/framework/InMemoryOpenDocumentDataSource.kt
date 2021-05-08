package com.raywenderlich.android.majesticreader.framework

import com.raywenderlich.android.majesticreader.data.OpenDocumentDataSource
import com.raywenderlich.android.majesticreader.domain.Document

class InMemoryOpenDocumentDataSource : OpenDocumentDataSource {

    private var openDocument: Document = Document.EMPTY;

    override fun setOpenDocument(document: Document) {
        openDocument = document
    }

    override fun getOpenDocument(): Document {
        return openDocument
    }
}