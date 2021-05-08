package com.raywenderlich.android.majesticreader.interactors

import com.raywenderlich.android.majesticreader.data.DocumentRepository

class GetOpenDocument(private val documentRepository: DocumentRepository) {
    operator fun invoke() = documentRepository.getOpenDocument()
}