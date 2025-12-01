
package io.github.swiftcut.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ProjectStorage {
    fun copyVideoToProject(
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

            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
