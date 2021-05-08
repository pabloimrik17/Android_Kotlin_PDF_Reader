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

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.android.majesticreader.R
import com.raywenderlich.android.majesticreader.domain.Document
import com.raywenderlich.android.majesticreader.framework.MajesticViewModelFactory
import com.raywenderlich.android.majesticreader.presentation.IntentUtil
import com.raywenderlich.android.majesticreader.presentation.library.LibraryFragment
import kotlinx.android.synthetic.main.fragment_reader.*

class ReaderFragment : Fragment() {

  companion object {

    fun newInstance(document: Document) = ReaderFragment().apply {
      arguments = ReaderViewModel.createArguments(document)
    }
  }

  private lateinit var viewModel: ReaderViewModel

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_reader, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val adapter = BookmarksAdapter {
      viewModel.openBookmark(it)
    }
    bookmarksRecyclerView.adapter = adapter

    viewModel = ViewModelProviders.of(this, MajesticViewModelFactory)
        .get(ReaderViewModel::class.java)

    viewModel.document.observe(this, Observer {
      if (it == Document.EMPTY) {
        // Show file picker action.
        startActivityForResult(IntentUtil.createOpenIntent(), LibraryFragment.READ_REQUEST_CODE)
      }
    })

    viewModel.bookmarks.observe(this, Observer {
      adapter.update(it)
    })

    viewModel.isBookmarked.observe(this, Observer {
      val bookmarkDrawable = if (it) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
      tabBookmark.setCompoundDrawablesWithIntrinsicBounds(0, bookmarkDrawable, 0, 0)
    })

    viewModel.isInLibrary.observe(this, Observer {
      val libraryDrawable = if(it) R.drawable.ic_library else R.drawable.ic_library_border
      tabLibrary.setCompoundDrawablesRelativeWithIntrinsicBounds(0, libraryDrawable, 0, 0)
    })

    viewModel.currentPage.observe(this, Observer { showPage(it) })
    viewModel.hasNextPage.observe(this, Observer { tabNextPage.isEnabled = it })
    viewModel.hasPreviousPage.observe(this, Observer { tabPreviousPage.isEnabled = it })
    
    if(savedInstanceState == null) {
      viewModel.loadArguments(arguments)
    } else {
      // Recreating fragment after configuration change, reopen current page so it can be rendered again.
      viewModel.reopenPage()
    }

    tabBookmark.setOnClickListener { viewModel.toggleBookmark() }
    tabLibrary.setOnClickListener { viewModel.toggleInLibrary() }
    tabNextPage.setOnClickListener { viewModel.nextPage() }
    tabPreviousPage.setOnClickListener { viewModel.previousPage() }
  }

  private fun showPage(page: PdfRenderer.Page) {
    iv_page.visibility = View.VISIBLE
    pagesTextView.visibility = View.VISIBLE
    tabPreviousPage.visibility = View.VISIBLE
    tabNextPage.visibility = View.VISIBLE

    if (iv_page.drawable != null) {
      (iv_page.drawable as BitmapDrawable).bitmap.recycle()
    }

    val size = Point()
    activity?.windowManager?.defaultDisplay?.getSize(size)

    val pageWidth = size.x
    val pageHeight = page.height * pageWidth / page.width

    val bitmap = Bitmap.createBitmap(
        pageWidth,
        pageHeight,
        Bitmap.Config.ARGB_8888)

    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    iv_page.setImageBitmap(bitmap)

    pagesTextView.text = getString(
        R.string.page_navigation_format,
        page.index + 1,
        viewModel.renderer.value?.pageCount
    )

    page.close()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    // Process open file intent.
    if (requestCode == LibraryFragment.READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      data?.data?.also { uri -> viewModel.openDocument(uri) }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

}
