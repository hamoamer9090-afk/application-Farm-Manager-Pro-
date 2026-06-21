package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    fun generateAndSharePdf(context: Context, title: String, content: String, imagesBase64: List<String>) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
        }

        val contentPaint = TextPaint().apply {
            color = Color.BLACK
            textSize = 16f
        }

        var yPos = 40f
        val margin = 40f
        val maxWidth = pageInfo.pageWidth - (margin * 2)

        // Draw title
        val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, titlePaint, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        
        canvas.save()
        canvas.translate(margin, yPos)
        titleLayout.draw(canvas)
        canvas.restore()
        yPos += titleLayout.height + 20f

        // Draw content
        val contentLayout = StaticLayout.Builder.obtain(content, 0, content.length, contentPaint, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
            
        canvas.save()
        canvas.translate(margin, yPos)
        contentLayout.draw(canvas)
        canvas.restore()
        yPos += contentLayout.height + 40f

        // Draw images
        imagesBase64.forEach { base64 ->
            val bitmap = ImageUtils.base64ToBitmap(base64)
            if (bitmap != null) {
                // scale bitmap to fit page width
                val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                val targetWidth = maxWidth
                var targetHeight = targetWidth * aspectRatio

                // Scale down if image is taller than page
                if (targetHeight > pageInfo.pageHeight - (margin * 2)) {
                    targetHeight = pageInfo.pageHeight - (margin * 2)
                    // adjust width proportionally
                    // targetWidth = targetHeight / aspectRatio // Not strictly necessary if we just center it or align left, but good to know
                }

                if (yPos + targetHeight > pageInfo.pageHeight - margin) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = margin
                }

                canvas.drawBitmap(bitmap, null, android.graphics.RectF(margin, yPos, margin + (targetHeight / aspectRatio), yPos + targetHeight), null)
                yPos += targetHeight + 20f
            }
        }

        document.finishPage(page)

        try {
            val file = File(context.cacheDir, "Note_${System.currentTimeMillis()}.pdf")
            val out = FileOutputStream(file)
            document.writeTo(out)
            document.close()
            out.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "مشاركة الملاحظة"))
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
        }
    }
}
