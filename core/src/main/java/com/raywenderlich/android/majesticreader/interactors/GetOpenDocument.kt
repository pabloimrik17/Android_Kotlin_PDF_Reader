package com.raywenderlich.android.majesticreader.interactors

import com.raywenderlich.android.majesticreader.data.DocumentRepository

class GetOpenDocument(private val documentRepository: DocumentRepository) {
    suspend operator fun invoke() = documentRepository.getOpenDocument()
}