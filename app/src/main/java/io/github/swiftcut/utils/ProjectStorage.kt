
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

            val fileName = "imported_video.mp4"
            val destFile = File(projectDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (destFile != null) {
                val result = NativeLib.extractThumbnails(destFile.absolutePath, projectDir.absolutePath + "/thumbnails")
            }

            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getThumbDir(context: Context, projectName: String = "project_1"): File {
        return File(context.filesDir, "projects/${projectName}/thumbnails")
    }
}
