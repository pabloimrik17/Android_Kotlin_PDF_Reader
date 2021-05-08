/*
 * Copyright (c) 2019 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.majesticreader.presentation.reader

import android.app.Application
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.raywenderlich.android.majesticreader.domain.Bookmark
import com.raywenderlich.android.majesticreader.domain.Document
import com.raywenderlich.android.majesticreader.framework.Interactors
import com.raywenderlich.android.majesticreader.framework.MajesticViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

class ReaderViewModel(application: Application, interactors: Interactors) : MajesticViewModel
(application, interactors) {

  companion object {
    private const val DOCUMENT_ARG = "document"

    fun createArguments(document: Document) = bundleOf(
        DOCUMENT_ARG to document
    )
  }

  val document = MutableLiveData<Document>()

  val bookmarks = MediatorLiveData<List<Bookmark>>().apply {
    addSource(document) { document ->
      GlobalScope.launch {
        postValue(interactors.getBookmarks(document))
      }
    }
  }

  val currentPage = MediatorLiveData<PdfRenderer.Page>()

  val hasPreviousPage: LiveData<Boolean> = Transformations.map(currentPage) {
    it.index > 0
  }

  val hasNextPage: LiveData<Boolean> = Transformations.map(currentPage) {
    renderer.value?.let { renderer -> it.index < renderer.pageCount - 1 }
  }

  val isBookmarked = MediatorLiveData<Boolean>().apply {
    addSource(document) { value = isCurrentPageBookmarked() }
    addSource(currentPage) { value = isCurrentPageBookmarked() }
    addSource(bookmarks) { value = isCurrentPageBookmarked() }
  }

  val isInLibrary: MediatorLiveData<Boolean> = MediatorLiveData<Boolean>().apply {
    addSource(document) { document -> GlobalScope.launch {
      postValue(isInLibrary(document))
    } }
  }

  val renderer = MediatorLiveData<PdfRenderer>().apply {
    addSource(document) {
      try {
        val pdfRenderer = PdfRenderer(getFileDescriptor(Uri.parse(it.url)))
        value = pdfRenderer
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  private fun getFileDescriptor(uri: Uri) = application.contentResolver.openFileDescriptor(uri, "r")

  private fun isCurrentPageBookmarked() =
      bookmarks.value?.any { it.page == currentPage.value?.index } == true


  private suspend fun isInLibrary(document: Document) = interactors.getDocuments().any { it.url == document.url }

  fun loadArguments(arguments: Bundle?) {
    if (arguments == null) {
      return
    }

    currentPage.apply {
      addSource(renderer) { renderer ->
        GlobalScope.launch {
          val document = document.value

          if (document != null) {
            val bookmarks = interactors.getBookmarks(document).lastOrNull()?.page ?: 0

            postValue(renderer.openPage(bookmarks))
          }
        }
      }
    }

    val documentFromArguments = arguments.get(DOCUMENT_ARG) as Document? ?: Document.EMPTY

    val lastOpenDocument = interactors.getOpenDocument()

    document.value = when {
      documentFromArguments != Document.EMPTY -> documentFromArguments
      documentFromArguments == Document.EMPTY && lastOpenDocument !== Document.EMPTY -> lastOpenDocument
      else -> Document.EMPTY
    }

    document.value?.let { interactors.setOpenDocument(it) }


  }

  fun openDocument(uri: Uri) {
    document.value = Document(uri.toString(), "", 0, "")
    document.value?.let { interactors.setOpenDocument(it) }
  }

  fun openBookmark(bookmark: Bookmark) {
    openPage(bookmark.page)
  }

  private fun openPage(page: Int) = renderer.value?.let {
    currentPage.value = it.openPage(page)
  }

  fun nextPage() = currentPage.value?.let { openPage(it.index.plus(1)) }

  fun previousPage() = currentPage.value?.let { openPage(it.index.minus(1)) }

  fun reopenPage() = openPage(currentPage.value?.index ?: 0)

  fun toggleBookmark() {
    val currentPage = currentPage.value?.index ?: return
    val document = document.value ?: return
    val bookmark = bookmarks.value?.firstOrNull() { it.page == currentPage }

    GlobalScope.launch {
      if (bookmark == null) {
        interactors.addBookmark(document, Bookmark(page = currentPage))
      } else {
        interactors.deleteBookmark(document, bookmark)
      }

      bookmarks.postValue(interactors.getBookmarks(document))
    }
  }

  fun toggleInLibrary() {
    val document = document.value ?: return

    GlobalScope.launch {
      if (isInLibrary.value == true) {
        interactors.removeDocument(document)
      } else {
        interactors.addDocument(document)
      }

      isInLibrary.postValue(isInLibrary(document))
    }
  }
}
