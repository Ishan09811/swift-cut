
package io.github.swiftcut.utils

import android.content.Context
import android.net.Uri
import io.github.swiftcut.NativeLib
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Serializable
data class ProjectVideo(
    val path: String,
    val durationMs: Long,
    val thumbDir: String
)

@Serializable
data class Transition(
    val type: String,
    val durationMs: Long
)

@Serializable
data class Project(
    val name: String,
    val videos: MutableList<ProjectVideo> = mutableListOf(),
    val transitions: MutableList<Transition?> = mutableListOf()
)

object ProjectStorage {
    
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
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

            if (destFile != null && !File(projectDir, "thumbnails/${fileName.substring(0, fileName.lastIndexOf('.'))}").exists()) {
                // TODO: move to somewhere else to avoid blocking ui
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

        val cursor = context.contentResolver.query(
            uri,
            arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = it.getString(index)
                }
            }
        }

        return name
    }

    private fun projectsFile(context: Context): File {
        return File(context.filesDir, "projects.json")
    }

    fun loadProjects(context: Context): List<Project> {
        return try {
            val file = projectsFile(context)
            if (!file.exists()) return emptyList()

            val content = file.readText()
            json.decodeFromString(content)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveProjects(context: Context, projects: List<Project>) {
        try {
            val file = projectsFile(context)
            val content = json.encodeToString(projects)
            file.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveProject(context: Context, project: Project) {
        try {
            val existing = loadProjects(context).toMutableList()

            val index = existing.indexOfFirst { it.name == project.name }
            if (index >= 0) {
                existing[index] = project
            } else {
                existing.add(project)
            }

            saveProjects(context, existing)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun File?.nameWithoutExtension(): String {
        if (this == null) return "imported_video"
        val name = this.name
        val lastDot = name.lastIndexOf('.')
        return if (lastDot != -1) name.substring(0, lastDot) else name
    }
}
