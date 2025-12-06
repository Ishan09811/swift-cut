
package io.github.swiftcut.utils

import android.content.Context
import android.net.Uri
import io.github.swiftcut.NativeLib
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ProjectStorage {
    
    fun importVideo(
        context: Context,
        sourceUri: Uri,
        projectName: String = "project_1"
    ): File? {
        return try {
            val projectDir = File(context.filesDir, "projects/$projectName")
            if (!projectDir.exists()) projectDir.mkdirs()

            val fileName = getFileNameFromUri(context, sourceUri)
            val destFile = File(projectDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile != null) {
                val result = NativeLib.extractThumbnails(destFile.absolutePath, projectDir.absolutePath + "/thumbnails/${fileName.substring(0, fileName.lastIndexOf('.'))}")
            }

            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getThumbDir(context: Context, videoName: String, projectName: String = "project_1"): File {
        return File(context.filesDir, "projects/${projectName}/thumbnails/$videoName")
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var name = "imported_video.mp4"

        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndexOpenable(android.provider.OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }

        return name
    }

    fun File?.nameWithoutExtension(): String {
        if (this == null) return "imported_video"
        val name = this.name
        val lastDot = name.lastIndexOf('.')
        return if (lastDot != -1) name.substring(0, lastDot) else name
    }
}
