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

package com.raywenderlich.android.majesticreader.framework

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {

  data class DocumentDetail(val name: String, val size: Int, val thumbnail: String)

  private fun getPdfThumbnailUri(context: Context, documentUri: String, documentName: String):
      String {
    val pdfRenderer = PdfRenderer(context.contentResolver.openFileDescriptor(Uri.parse(documentUri),
        "r"))

    val firstPage = pdfRenderer.openPage(0)

    val bitmap = Bitmap.createBitmap(
        firstPage.width,
        firstPage.height,
        Bitmap.Config.ARGB_8888)

    firstPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    firstPage.close()
    pdfRenderer.close()

    // Write bitmap
    val thumbnailFile = File(context.cacheDir, documentName + "_thumbnail")

    try {
      FileOutputStream(thumbnailFile).use { out: FileOutputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }

    return thumbnailFile.absolutePath
  }

  fun getDocumentDetails(context: Context, documentUri: String): DocumentDetail {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE)

    context.contentResolver.query(Uri.parse(documentUri), projection, null, null, null)?.use {
      metaCursor ->
      val nameIndex = metaCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      val sizeIndex = metaCursor.getColumnIndex(OpenableColumns.SIZE)

      return if (metaCursor.moveToFirst()) {
        val name = metaCursor.getString(nameIndex)

        DocumentDetail(
            name,
            metaCursor.getInt(sizeIndex),
            getPdfThumbnailUri(context,
                documentUri, name)
        )
      } else {
        DocumentDetail(
            "No name",
            0,
            ""
        )
      }
    } ?: return DocumentDetail(
        "No name", 0, "")
  }

}