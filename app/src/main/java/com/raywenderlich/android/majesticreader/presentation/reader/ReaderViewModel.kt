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
    // TODO add sources
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
    addSource(document) { value = isInLibrary(it) }
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

  // TODO check if document is in library
  private fun isInLibrary(document: Document) = false

  fun loadArguments(arguments: Bundle?) {
    if (arguments == null) {
      return
    }

    // TODO load document from arguments and initialize
  }

  fun openDocument(uri: Uri) {
    // TODO open document
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
    // TODO toggle bookmark on the current page
  }

  fun toggleInLibrary() {
    // TODO toggle if open document is in library
  }
}
