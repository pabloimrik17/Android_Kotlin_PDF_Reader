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

package com.raywenderlich.android.majesticreader.presentation.library

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.raywenderlich.android.majesticreader.R
import com.raywenderlich.android.majesticreader.framework.MajesticViewModelFactory
import com.raywenderlich.android.majesticreader.presentation.IntentUtil.createOpenIntent
import com.raywenderlich.android.majesticreader.presentation.MainActivityDelegate
import kotlinx.android.synthetic.main.fragment_library.*

class LibraryFragment : Fragment() {

  companion object {
    const val READ_REQUEST_CODE = 100

    fun newInstance() = LibraryFragment()
  }

  private lateinit var viewModel: LibraryViewModel

  private lateinit var mainActivityDelegate: MainActivityDelegate

  override fun onAttach(context: Context?) {
    super.onAttach(context)

    try {
      mainActivityDelegate = context as MainActivityDelegate
    } catch (e: ClassCastException) {
      throw ClassCastException()
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_library, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val adapter = DocumentsAdapter(glide = Glide.with(this)) {
      mainActivityDelegate.openDocument(it)
    }
    documentsRecyclerView.adapter = adapter

    viewModel = ViewModelProviders.of(this, MajesticViewModelFactory)
        .get(LibraryViewModel::class.java)
    viewModel.documents.observe(this, Observer { adapter.update(it) })
    viewModel.loadDocuments()

    fab.setOnClickListener { startActivityForResult(createOpenIntent(), READ_REQUEST_CODE) }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    // Process open file intent.
    if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      data?.data?.also { uri -> viewModel.addDocument(uri) }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }

}
