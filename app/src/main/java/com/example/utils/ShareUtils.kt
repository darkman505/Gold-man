package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter

object ShareUtils {

    fun shareText(context: Context, title: String, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة عبر..."))
        } catch (e: Exception) {
            Toast.makeText(context, "❌ حدث خطأ أثناء المشاركة: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, fileName: String, fileContent: String, mimeType: String = "text/plain") {
        try {
            val cachePath = File(context.cacheDir, "shared")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }
            val file = File(cachePath, fileName)
            val writer = FileWriter(file)
            writer.write(fileContent)
            writer.close()

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "com.example.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "حفظ أو مشاركة الملف..."))
        } catch (e: Exception) {
            Toast.makeText(context, "❌ حدث خطأ أثناء حفظ ومشاركة الملف: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
