package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object ShareUtil {
    fun shareAppApk(context: Context) {
        try {
            // 1. Get the path to the current APK
            val apkPath = context.packageCodePath
            val originalFile = File(apkPath)
            
            // 2. Copy the APK to cache directory to ensure accessibility
            val cacheDir = File(context.cacheDir, "shared_apk")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            
            val destinationFile = File(cacheDir, "${context.getString(context.applicationInfo.labelRes)}.apk")
            originalFile.copyTo(destinationFile, overwrite = true)

            // 3. Create a URI using FileProvider
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                destinationFile
            )

            // 4. Create the Share Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 5. Launch the system share sheet
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التطبيق (APK)"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
